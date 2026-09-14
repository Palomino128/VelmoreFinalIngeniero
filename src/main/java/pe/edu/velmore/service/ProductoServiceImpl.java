package pe.edu.velmore.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.edu.velmore.dao.ProductoDao;
import pe.edu.velmore.dto.BusquedaProductoDto;
import pe.edu.velmore.model.Categoria;
import pe.edu.velmore.model.Producto;
import pe.edu.velmore.validation.ProductoValidator;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de productos VELMORE.
 *
 * <h3>Arquitectura Híbrida: MySQL + Google Guava LoadingCache</h3>
 * <p>El servicio recibe {@link ProductoDao} como abstracción de persistencia:</p>
 * <ul>
 *   <li>En <b>producción</b> Spring inyecta {@code ProductoRepositoryAdapter}
 *       (marcado con {@code @Primary}), que delega en JPA/MySQL.</li>
 *   <li>En <b>tests unitarios</b> se instancia con {@code InMemoryProductoDao},
 *       sin necesitar contexto Spring ni base de datos.</li>
 * </ul>
 *
 * <h3>Cachés Guava configuradas</h3>
 * <ul>
 *   <li>{@code cachePorId}   — {@link LoadingCache}: carga automática por ID.</li>
 *   <li>{@code cacheCatalogo} — caché del listado completo de productos activos.</li>
 * </ul>
 *
 * <h3>Política de coherencia</h3>
 * <p>Toda escritura persiste primero en la BD y luego invalida/actualiza la caché.</p>
 */
@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);
    private static final String CACHE_KEY_ACTIVOS = "catalogo_activos";

    // ── Dependencias ──────────────────────────────────────────────────────────
    private final ProductoDao productoDao;
    private final ProductoValidator productoValidator;

    // ── Google Guava: LoadingCache por ID ────────────────────────────────────
    // Máximo 100 entradas, expira 10 minutos tras la última escritura.
    // CacheLoader auto-invoca la BD solo cuando hay MISS.
    private final LoadingCache<Long, Producto> cachePorId;

    // ── Google Guava: Cache para el catálogo completo ────────────────────────
    private final com.google.common.cache.Cache<String, List<Producto>> cacheCatalogo;

    public ProductoServiceImpl(ProductoDao productoDao, ProductoValidator productoValidator) {
        this.productoDao       = productoDao;
        this.productoValidator = productoValidator;

        // LoadingCache: define cómo cargar un producto si no está en caché
        this.cachePorId = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build(new CacheLoader<Long, Producto>() {
                    @Override
                    public Producto load(Long id) {
                        log.info("[CACHE MISS] ID={} no en caché → consultando BD", id);
                        return productoDao.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Producto no encontrado con ID: " + id));
                    }
                });

        this.cacheCatalogo = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    // ── LECTURAS ──────────────────────────────────────────────────────────────

    @Override
    public List<Producto> listarActivos() {
        List<Producto> enCache = cacheCatalogo.getIfPresent(CACHE_KEY_ACTIVOS);
        if (enCache != null) {
            log.info("[CACHE HIT] Catálogo desde Guava. Stats: {}", cacheCatalogo.stats());
            return enCache;
        }
        log.info("[CACHE MISS] Cargando catálogo desde BD...");
        List<Producto> activos = productoDao.findAll().stream()
                .filter(Producto::isActivo)
                .sorted(Comparator.comparing(Producto::getNombre))
                .collect(Collectors.toList());
        // ImmutableList: garantiza inmutabilidad de la lista en caché
        cacheCatalogo.put(CACHE_KEY_ACTIVOS, ImmutableList.copyOf(activos));
        log.info("[CACHE STORE] {} productos activos en caché", activos.size());
        return activos;
    }

    @Override
    public Producto obtenerPorId(Long id) {
        try {
            // HIT → retorna desde RAM. MISS → CacheLoader consulta la BD automáticamente.
            Producto p = cachePorId.get(id);
            log.info("[CACHE] obtenerPorId({}) Stats: {}", id, cachePorId.stats());
            return p;
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            if (causa instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) causa;
            }
            throw new RuntimeException("Error al obtener producto ID=" + id, e);
        }
    }

    @Override
    public List<Producto> buscar(BusquedaProductoDto filtro) {
        String texto  = filtro == null ? "" : StringUtils.trimToEmpty(filtro.getTexto()).toLowerCase();
        Categoria cat = filtro == null ? null : filtro.getCategoria();

        return listarActivos().stream()
                .filter(p -> StringUtils.isBlank(texto)
                        || p.getNombre().toLowerCase().contains(texto)
                        || p.getMarca().toLowerCase().contains(texto)
                        || p.getDescripcion().toLowerCase().contains(texto))
                .filter(p -> cat == null || p.getCategoria() == cat)
                .collect(Collectors.toList());
    }

    // ── ESCRITURAS (BD primero → caché después) ───────────────────────────────

    @Override
    public Producto registrar(Producto producto) {
        productoValidator.validar(producto);
        producto.setNombre(StringUtils.trimToEmpty(producto.getNombre()));
        producto.setMarca(StringUtils.trimToEmpty(producto.getMarca()));
        producto.setActivo(true);

        Producto guardado = productoDao.save(producto);            // 1. Persiste en BD
        cachePorId.put(guardado.getId(), guardado);               // 2. Carga en caché por ID
        cacheCatalogo.invalidate(CACHE_KEY_ACTIVOS);              // 3. Invalida catálogo

        log.info("[DB+CACHE] Registrado: '{}' ID={}. Catálogo invalidado.",
                guardado.getNombre(), guardado.getId());
        return guardado;
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {
        obtenerPorId(id); // Verifica existencia (y calienta caché)
        producto.setId(id);
        producto.setNombre(StringUtils.trimToEmpty(producto.getNombre()));
        producto.setMarca(StringUtils.trimToEmpty(producto.getMarca()));
        producto.setActivo(true);
        productoValidator.validar(producto);

        Producto actualizado = productoDao.save(producto);        // 1. Persiste en BD
        cachePorId.put(id, actualizado);                          // 2. Actualiza caché por ID
        cacheCatalogo.invalidate(CACHE_KEY_ACTIVOS);              // 3. Invalida catálogo

        log.info("[DB+CACHE] Actualizado: '{}' ID={}. Caché sincronizada.",
                actualizado.getNombre(), id);
        return actualizado;
    }

    @Override
    public void eliminar(Long id) {
        obtenerPorId(id);                                         // Verifica existencia
        productoDao.deleteById(id);                               // 1. Elimina de BD
        cachePorId.invalidate(id);                                // 2. Expulsa de caché por ID
        cacheCatalogo.invalidate(CACHE_KEY_ACTIVOS);              // 3. Invalida catálogo

        log.warn("[DB+CACHE] Eliminado ID={}. Expulsado de ambas cachés.", id);
    }

    // ── MÉTRICAS (resueltas desde caché, sin tocar la BD) ────────────────────

    @Override
    public int totalProductos() {
        return listarActivos().size();
    }

    @Override
    public long totalCategorias() {
        return listarActivos().stream()
                .map(Producto::getCategoria)
                .distinct()
                .count();
    }

    @Override
    public double valorInventario() {
        return listarActivos().stream()
                .mapToDouble(p -> p.getPrecio() * p.getStock())
                .sum();
    }

    // ── ACCESO A ESTADÍSTICAS GUAVA (para monitoreo) ────────────────────────

    /**
     * Retorna las estadísticas de la caché por ID.
     * Usadas por {@link pe.edu.velmore.monitor.GuavaCacheHealthIndicator}
     * y {@link pe.edu.velmore.monitor.VelmoreMicrometerMetrics}.
     */
    public CacheStats getStatsCachePorId() {
        return cachePorId.stats();
    }

    /**
     * Retorna las estadísticas de la caché del catálogo completo.
     */
    public CacheStats getStatsCacheCatalogo() {
        return cacheCatalogo.stats();
    }

    /**
     * Retorna el número de entradas actuales en la caché por ID.
     */
    public long getCachePorIdSize() {
        return cachePorId.size();
    }

    /**
     * Invalida completamente ambas cachés.
     * Usado por los procesos de mantenimiento (Cron Jobs) para liberar RAM
     * o asegurar consistencia forzada con la base de datos de madrugada.
     */
    public void limpiarCaches() {
        cachePorId.invalidateAll();
        cacheCatalogo.invalidateAll();
        log.warn("[MANTENIMIENTO] Cachés de Guava (ID y Catálogo) han sido completamente invalidadas.");
    }
}

