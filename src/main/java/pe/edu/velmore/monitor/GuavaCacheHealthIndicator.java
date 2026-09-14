package pe.edu.velmore.monitor;

import com.google.common.cache.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import pe.edu.velmore.service.ProductoService;
import pe.edu.velmore.service.ProductoServiceImpl;

/**
 * CAPA 2 — Health Indicator personalizado para el caché Guava.
 *
 * <p>Expone el estado del caché Guava de productos en el endpoint:
 * {@code GET /actuator/health/guavacache}</p>
 *
 * <h3>Umbrales de salud:</h3>
 * <ul>
 *   <li><b>UP</b>   — hit rate ≥ 30% o caché aún vacío (arranque)</li>
 *   <li><b>DOWN</b> — hit rate &lt; 30% con más de 50 peticiones (ineficiencia)</li>
 * </ul>
 *
 * <h3>Métricas expuestas:</h3>
 * <ul>
 *   <li>hitRate      — proporción de peticiones servidas desde RAM</li>
 *   <li>hitCount     — total de aciertos de caché</li>
 *   <li>missCount    — total de fallos (consultas a la BD)</li>
 *   <li>loadCount    — veces que CacheLoader consultó la BD</li>
 *   <li>evictionCount— entradas expulsadas por expiración</li>
 *   <li>cacheSize    — entradas actuales en la caché por ID</li>
 * </ul>
 */
@Component("guavacache")
public class GuavaCacheHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(GuavaCacheHealthIndicator.class);
    private static final double UMBRAL_HIT_RATE_MINIMO = 0.30; // 30%
    private static final long   PETICIONES_MINIMAS     = 50L;  // evitar falsos positivos al arranque

    private final ProductoServiceImpl productoServiceImpl;

    /**
     * Inyección del servicio concreto para acceder a las estadísticas de Guava.
     * Se usa ProductoServiceImpl directamente para acceder a los CacheStats.
     */
    public GuavaCacheHealthIndicator(ProductoService productoService) {
        this.productoServiceImpl = (ProductoServiceImpl) productoService;
    }

    @Override
    public Health health() {
        try {
            CacheStats statsPorId     = productoServiceImpl.getStatsCachePorId();
            CacheStats statsCatalogo  = productoServiceImpl.getStatsCacheCatalogo();
            long       totalPeticiones = statsPorId.requestCount();
            double     hitRate         = statsPorId.hitRate();
            long       cacheSize       = productoServiceImpl.getCachePorIdSize();

            log.info("[MONITOR] Guava cachePorId — hitRate={:.1%}, size={}, misses={}, loads={}",
                    hitRate, cacheSize, statsPorId.missCount(), statsPorId.loadCount());

            Health.Builder builder;

            // Sistema de semáforo: DOWN si hit rate bajo con suficientes peticiones
            if (totalPeticiones >= PETICIONES_MINIMAS && hitRate < UMBRAL_HIT_RATE_MINIMO) {
                log.warn("[MONITOR] Caché Guava con hit rate bajo: {:.1%} < {:.1%}",
                        hitRate, UMBRAL_HIT_RATE_MINIMO);
                builder = Health.down();
            } else {
                builder = Health.up();
            }

            return builder
                    // ── cachePorId (LoadingCache por producto individual) ──
                    .withDetail("cachePorId.hitRate",      String.format("%.1f%%", hitRate * 100))
                    .withDetail("cachePorId.hitCount",     statsPorId.hitCount())
                    .withDetail("cachePorId.missCount",    statsPorId.missCount())
                    .withDetail("cachePorId.loadCount",    statsPorId.loadCount())
                    .withDetail("cachePorId.evictions",    statsPorId.evictionCount())
                    .withDetail("cachePorId.size",         cacheSize)
                    .withDetail("cachePorId.avgLoadMs",    String.format("%.2f ms",
                            statsPorId.averageLoadPenalty() / 1_000_000.0))
                    // ── cacheCatalogo (listado completo de activos) ───────
                    .withDetail("cacheCatalogo.hitRate",   String.format("%.1f%%",
                            statsCatalogo.hitRate() * 100))
                    .withDetail("cacheCatalogo.hitCount",  statsCatalogo.hitCount())
                    .withDetail("cacheCatalogo.missCount", statsCatalogo.missCount())
                    // ── Evaluación general ────────────────────────────────
                    .withDetail("umbralHitRate",           String.format("%.0f%%",
                            UMBRAL_HIT_RATE_MINIMO * 100))
                    .withDetail("peticionesAnalizadas",    totalPeticiones)
                    .withDetail("estado",  totalPeticiones < PETICIONES_MINIMAS
                            ? "CALENTANDO (pocas peticiones aún)"
                            : hitRate >= UMBRAL_HIT_RATE_MINIMO ? "ÓPTIMO" : "INEFICIENTE")
                    .build();

        } catch (Exception e) {
            log.error("[MONITOR] Error al evaluar salud del caché Guava: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
