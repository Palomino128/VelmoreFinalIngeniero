package pe.edu.velmore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.velmore.service.ProductoService;
import pe.edu.velmore.service.ProductoServiceImpl;

/**
 * Taller de Mantenimiento — Tareas Programadas (Cron Jobs).
 *
 * <p>Esta clase define rutinas automáticas de mantenimiento que se ejecutan
 * en segundo plano usando el planificador de Spring Boot.</p>
 */
@Component
public class TareasMantenimiento {

    private static final Logger log = LoggerFactory.getLogger(TareasMantenimiento.class);
    private final ProductoServiceImpl productoServiceImpl;

    public TareasMantenimiento(ProductoService productoService) {
        this.productoServiceImpl = (ProductoServiceImpl) productoService;
    }

    /**
     * CRON JOB: Refresco de Caché
     * Se ejecuta todos los días a las 03:00 AM.
     * Propósito: Liberar RAM y forzar que al día siguiente la app cargue 
     * el catálogo directamente de la BD, evitando problemas de estancamiento de memoria.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void rutinaMadrugada() {
        log.info("[CRON] 03:00 AM — Iniciando rutina de mantenimiento de madrugada...");
        
        // Se limpian explícitamente los cachés Guava
        productoServiceImpl.limpiarCaches();
        
        log.info("[CRON] 03:00 AM — Rutina completada. Cachés vaciados.");
    }

    /**
     * CRON JOB: Reporte Ejecutivo
     * Se ejecuta todos los días a las 08:00 AM (inicio de jornada).
     * Propósito: Imprimir un resumen del estado del negocio en los logs para 
     * que los administradores tengan una visión rápida diaria.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void reporteMatutino() {
        int totalProductos = productoServiceImpl.totalProductos();
        double valorInventario = productoServiceImpl.valorInventario();
        long cacheSize = productoServiceImpl.getCachePorIdSize();
        
        log.info("==========================================================");
        log.info("[CRON] REPORTE MATUTINO (08:00 AM)");
        log.info("  - Productos Activos : {}", totalProductos);
        log.info("  - Valor Inventario  : S/ {}", String.format("%.2f", valorInventario));
        log.info("  - Entradas en Caché : {}", cacheSize);
        log.info("==========================================================");
    }
}
