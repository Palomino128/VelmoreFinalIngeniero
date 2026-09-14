package pe.edu.velmore.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.edu.velmore.model.Producto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 *  HOTFIX: AlertaStockService
 *  Rama: hotfix/stock-critico
 * ============================================================
 *  Parche urgente: se detectó que productos con stock = 0
 *  seguían mostrándose en el catálogo sin aviso de agotado.
 *
 *  Este servicio centraliza las alertas de stock crítico
 *  para evitar vender productos sin disponibilidad.
 * ============================================================
 */
@Component
public class AlertaStockService {

    private static final Logger log = LoggerFactory.getLogger(AlertaStockService.class);

    /** Stock crítico: 0 unidades (agotado) */
    private static final int STOCK_AGOTADO   = 0;

    /** Stock bajo: menos de 5 unidades */
    private static final int STOCK_BAJO      = 5;

    /**
     * Retorna lista de productos AGOTADOS (stock = 0).
     * HOTFIX: antes no se filtraban correctamente.
     */
    public List<Producto> obtenerAgotados(List<Producto> productos) {
        List<Producto> agotados = productos.stream()
                .filter(p -> p.getStock() == STOCK_AGOTADO)
                .collect(Collectors.toList());

        if (!agotados.isEmpty()) {
            log.error("[HOTFIX][ALERTA] {} producto(s) AGOTADOS en catálogo:", agotados.size());
            agotados.forEach(p ->
                    log.error("  → ID={} '{}' stock={}", p.getId(), p.getNombre(), p.getStock()));
        }
        return agotados;
    }

    /**
     * Retorna lista de productos con stock bajo (> 0 pero < STOCK_BAJO).
     */
    public List<Producto> obtenerStockBajo(List<Producto> productos) {
        return productos.stream()
                .filter(p -> p.getStock() > STOCK_AGOTADO && p.getStock() < STOCK_BAJO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna true si el catálogo tiene productos agotados.
     * Retorna primitivo boolean.
     */
    public boolean hayProductosAgotados(List<Producto> productos) {
        boolean hayAgotados = productos.stream()
                .anyMatch(p -> p.getStock() == STOCK_AGOTADO);
        if (hayAgotados) {
            log.warn("[HOTFIX] ¡ALERTA! Hay productos agotados en el catálogo activo.");
        }
        return hayAgotados;
    }

    /**
     * Genera reporte de estado de stock del catálogo.
     */
    public String reporteStock(List<Producto> productos) {
        // Primitivos long para conteos
        long agotados  = productos.stream().filter(p -> p.getStock() == STOCK_AGOTADO).count();
        long stockBajo = productos.stream().filter(p -> p.getStock() > 0 && p.getStock() < STOCK_BAJO).count();
        long normal    = productos.size() - agotados - stockBajo;

        return String.format("[STOCK] Normal: %d | Bajo: %d | Agotado: %d | Total: %d",
                normal, stockBajo, agotados, productos.size());
    }
}
