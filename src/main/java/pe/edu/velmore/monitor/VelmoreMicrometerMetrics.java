package pe.edu.velmore.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.edu.velmore.service.ProductoService;
import pe.edu.velmore.service.ProductoServiceImpl;

import jakarta.annotation.PostConstruct;

/**
 * CAPA 3 — Métricas de negocio VELMORE con Micrometer.
 *
 * <p>Registra métricas observables desde Spring Actuator:
 * {@code GET /actuator/metrics}</p>
 *
 * <h3>Métricas registradas:</h3>
 * <table>
 *   <tr><th>Nombre</th><th>Tipo</th><th>Descripción</th></tr>
 *   <tr><td>velmore.productos.total</td><td>Gauge</td><td>Productos activos en catálogo</td></tr>
 *   <tr><td>velmore.inventario.valor</td><td>Gauge</td><td>Valor total del inventario (S/)</td></tr>
 *   <tr><td>velmore.cache.hit.rate</td><td>Gauge</td><td>Hit rate del caché Guava</td></tr>
 *   <tr><td>velmore.busquedas.total</td><td>Counter</td><td>Total búsquedas realizadas</td></tr>
 *   <tr><td>velmore.ordenes.whatsapp</td><td>Counter</td><td>Links WhatsApp generados</td></tr>
 *   <tr><td>velmore.detalle.visitas</td><td>Counter</td><td>Visitas a páginas de detalle</td></tr>
 * </table>
 *
 * <p><b>Consulta de ejemplo:</b></p>
 * <pre>GET /actuator/metrics/velmore.productos.total</pre>
 */
@Component
public class VelmoreMicrometerMetrics {

    private static final Logger log = LoggerFactory.getLogger(VelmoreMicrometerMetrics.class);

    private final MeterRegistry meterRegistry;
    private final ProductoServiceImpl productoServiceImpl;

    // ── Contadores — se incrementan en cada evento de negocio ─────────────────
    private Counter busquedasCounter;
    private Counter ordenesWhatsAppCounter;
    private Counter detalleVisitasCounter;

    public VelmoreMicrometerMetrics(MeterRegistry meterRegistry, ProductoService productoService) {
        this.meterRegistry       = meterRegistry;
        this.productoServiceImpl = (ProductoServiceImpl) productoService;
    }

    /**
     * Inicializa todos los Gauges y Counters al arrancar Spring.
     * Los Gauges son dinámicos: recalculan su valor en cada consulta a /actuator/metrics.
     */
    @PostConstruct
    public void inicializarMetricas() {

        // ── GAUGE: Total de productos activos en catálogo ─────────────────────
        // Se recalcula dinámicamente sin tocar la BD (usa la caché Guava)
        Gauge.builder("velmore.productos.total", productoServiceImpl,
                        svc -> svc.totalProductos())
                .description("Número total de productos activos en el catálogo VELMORE")
                .tag("entidad", "producto")
                .register(meterRegistry);

        // ── GAUGE: Valor monetario del inventario ─────────────────────────────
        Gauge.builder("velmore.inventario.valor", productoServiceImpl,
                        svc -> svc.valorInventario())
                .description("Valor total del inventario en soles (precio × stock de cada producto)")
                .tag("moneda", "PEN")
                .baseUnit("soles")
                .register(meterRegistry);

        // ── GAUGE: Hit rate del caché Guava cachePorId ────────────────────────
        // Mide qué proporción de consultas se resuelven desde RAM (sin ir a la BD)
        Gauge.builder("velmore.cache.hit.rate", productoServiceImpl,
                        svc -> svc.getStatsCachePorId().hitRate() * 100)
                .description("Hit rate del caché Guava por ID (%) — mayor es mejor")
                .tag("cache", "cachePorId")
                .baseUnit("porcentaje")
                .register(meterRegistry);

        // ── GAUGE: Tamaño actual de la caché por ID ───────────────────────────
        Gauge.builder("velmore.cache.size", productoServiceImpl,
                        svc -> svc.getCachePorIdSize())
                .description("Número de entradas actuales en la caché Guava por ID")
                .tag("cache", "cachePorId")
                .register(meterRegistry);

        // ── COUNTER: Búsquedas realizadas en el catálogo ─────────────────────
        busquedasCounter = Counter.builder("velmore.busquedas.total")
                .description("Número total de búsquedas realizadas por usuarios en el catálogo")
                .tag("origen", "catalogo")
                .register(meterRegistry);

        // ── COUNTER: Links de WhatsApp generados (órdenes de compra) ─────────
        ordenesWhatsAppCounter = Counter.builder("velmore.ordenes.whatsapp")
                .description("Número total de links de WhatsApp generados (intención de compra)")
                .tag("canal", "whatsapp")
                .register(meterRegistry);

        // ── COUNTER: Visitas a páginas de detalle de producto ─────────────────
        detalleVisitasCounter = Counter.builder("velmore.detalle.visitas")
                .description("Número de vistas a páginas de detalle de producto")
                .tag("pagina", "detalle")
                .register(meterRegistry);

        log.info("[MONITOR] Métricas Micrometer inicializadas: productos.total, inventario.valor, " +
                "cache.hit.rate, cache.size, busquedas.total, ordenes.whatsapp, detalle.visitas");
    }

    // ── Métodos públicos para incrementar contadores ──────────────────────────

    /**
     * Registra una búsqueda en el catálogo.
     * Llamar desde {@code CatalogoController.catalogo()}.
     */
    public void registrarBusqueda() {
        busquedasCounter.increment();
        log.debug("[MÉTRICA] busquedas.total = {}", (long) busquedasCounter.count());
    }

    /**
     * Registra la generación de un link de WhatsApp (intención de compra).
     * Llamar desde {@code CarritoController} o {@code CatalogoController}.
     */
    public void registrarOrdenWhatsApp() {
        ordenesWhatsAppCounter.increment();
        log.info("[MÉTRICA] ordenes.whatsapp = {} (nueva intención de compra)",
                (long) ordenesWhatsAppCounter.count());
    }

    /**
     * Registra una visita a la página de detalle de un producto.
     * Llamar desde {@code CatalogoController.detalle()}.
     */
    public void registrarVistaDetalle() {
        detalleVisitasCounter.increment();
        log.debug("[MÉTRICA] detalle.visitas = {}", (long) detalleVisitasCounter.count());
    }
}
