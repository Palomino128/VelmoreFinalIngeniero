package pe.edu.velmore.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.edu.velmore.model.Producto;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================================
 *  VELMORE — ProductoAnalisis (Utilidad de Catálogo)
 * ============================================================
 *  Esta clase demuestra los 4 conceptos fundamentales de Java:
 *
 *  1. TIPOS PRIMITIVOS : double, int, boolean, long
 *  2. MÉTODOS          : bloques de código propios de la clase
 *  3. FUNCIONES        : lambdas  ->  pasadas como parámetros
 *  4. MÉTODOS DE LENGUAJE : Stream API de Java (built-in)
 * ============================================================
 */
@Component
public class ProductoAnalisis {

    private static final Logger log = LoggerFactory.getLogger(ProductoAnalisis.class);

    // ─────────────────────────────────────────────────────────────
    // [1] TIPOS PRIMITIVOS — variables de valor simple
    // ─────────────────────────────────────────────────────────────

    /** Porcentaje mínimo de descuento para considerar un producto "en oferta" (primitivo double) */
    private static final double UMBRAL_OFERTA    = 0.15;   // 15 %

    /** Stock mínimo para alertar al administrador (primitivo int) */
    private static final int    STOCK_MINIMO     = 5;

    /** Precio máximo para la categoría "económico" (primitivo double) */
    private static final double PRECIO_ECONOMICO = 30.0;

    // ─────────────────────────────────────────────────────────────
    // [2] MÉTODOS — acciones propias de esta clase
    // ─────────────────────────────────────────────────────────────

    /**
     * Método: calcula el precio promedio del catálogo activo.
     * Retorna primitivo double.
     */
    public double calcularPrecioPromedio(List<Producto> productos) {
        if (productos == null || productos.isEmpty()) return 0.0;

        // [4] MÉTODO DE LENGUAJE: stream() → mapToDouble() → average()
        double promedio = productos.stream()
                .mapToDouble(Producto::getPrecio)   // [3] FUNCIÓN: referencia a método
                .average()
                .orElse(0.0);

        log.info("[ANALISIS] Precio promedio del catálogo: S/ {}", String.format("%.2f", promedio));
        return promedio;
    }

    /**
     * Método: cuenta cuántos productos tienen stock bajo.
     * Retorna primitivo int.
     */
    public int contarStockBajo(List<Producto> productos) {
        if (productos == null) return 0;

        // [4] MÉTODO DE LENGUAJE: stream() → filter() → count()
        // [3] FUNCIÓN (lambda): p -> p.getStock() < STOCK_MINIMO
        int cantidad = (int) productos.stream()
                .filter(p -> p.getStock() < STOCK_MINIMO)   // [3] lambda
                .count();

        if (cantidad > 0) {
            log.warn("[ANALISIS] {} producto(s) con stock bajo (< {})", cantidad, STOCK_MINIMO);
        }
        return cantidad;
    }

    /**
     * Método: devuelve el producto más caro del catálogo.
     * Retorna un objeto Optional<Producto>.
     */
    public Optional<Producto> productoMasCaro(List<Producto> productos) {
        if (productos == null || productos.isEmpty()) return Optional.empty();

        // [4] MÉTODO DE LENGUAJE: stream() → max()
        // [3] FUNCIÓN: Comparator.comparingDouble con referencia a método
        return productos.stream()
                .max(Comparator.comparingDouble(Producto::getPrecio));  // [3] función
    }

    /**
     * Método: devuelve el producto con menos stock (para alertas de admin).
     * Retorna Optional<Producto>.
     */
    public Optional<Producto> productoMenosStock(List<Producto> productos) {
        if (productos == null || productos.isEmpty()) return Optional.empty();

        // [4] MÉTODO DE LENGUAJE: stream() → min()
        return productos.stream()
                .min(Comparator.comparingInt(Producto::getStock));   // [3] función
    }

    /**
     * Método: filtra productos baratos (precio ≤ PRECIO_ECONOMICO).
     * Retorna List<Producto>.
     */
    public List<Producto> filtrarEconomicos(List<Producto> productos) {
        if (productos == null) return List.of();

        // [4] MÉTODOS DE LENGUAJE: stream() → filter() → sorted() → collect()
        // [3] FUNCIÓN (lambda): p -> p.getPrecio() <= PRECIO_ECONOMICO
        return productos.stream()
                .filter(p -> p.getPrecio() <= PRECIO_ECONOMICO)     // [3] lambda
                .sorted(Comparator.comparingDouble(Producto::getPrecio)) // [3] función
                .collect(Collectors.toList());
    }

    /**
     * Método: verifica si un producto tiene stock suficiente.
     * Retorna primitivo boolean.
     */
    public boolean tieneStockSuficiente(Producto producto) {
        // [1] PRIMITIVO boolean — resultado de comparar dos int
        boolean suficiente = producto.getStock() >= STOCK_MINIMO;
        log.debug("[ANALISIS] Producto '{}' — stock suficiente: {}", producto.getNombre(), suficiente);
        return suficiente;
    }

    /**
     * Método: calcula precio final aplicando descuento.
     * Retorna primitivo double.
     *
     * @param precioOriginal primitivo double
     * @param descuento      primitivo double (0.0 a 1.0)
     */
    public double aplicarDescuento(double precioOriginal, double descuento) {
        // [1] PRIMITIVOS: double precioOriginal, double descuento
        double precioFinal = precioOriginal * (1.0 - descuento);
        log.debug("[ANALISIS] Precio S/{} con {}% descuento → S/{}",
                precioOriginal, (int)(descuento * 100), String.format("%.2f", precioFinal));
        return precioFinal;
    }

    /**
     * Método: determina si el producto está en oferta según umbral.
     * Retorna primitivo boolean.
     */
    public boolean estaEnOferta(double precioOriginal, double precioActual) {
        // [1] PRIMITIVO double en operación aritmética
        double diferencia = precioOriginal - precioActual;
        // [1] PRIMITIVO boolean como resultado
        boolean enOferta = (diferencia / precioOriginal) >= UMBRAL_OFERTA;
        return enOferta;
    }

    /**
     * Método: genera resumen estadístico del catálogo.
     * Retorna String con estadísticas clave.
     */
    public String generarResumen(List<Producto> productos) {
        if (productos == null || productos.isEmpty()) return "Catálogo vacío.";

        // [1] PRIMITIVOS usados en cálculos locales
        int    total     = productos.size();
        double promedio  = calcularPrecioPromedio(productos);  // llama a otro método
        int    stockBajo = contarStockBajo(productos);          // llama a otro método

        // [4] MÉTODO DE LENGUAJE: stream() → mapToDouble() → sum()
        double valorTotal = productos.stream()
                .mapToDouble(p -> p.getPrecio() * p.getStock())  // [3] lambda
                .sum();

        // [4] MÉTODO DE LENGUAJE: stream() → map() → distinct() → count()
        long categorias = productos.stream()
                .map(Producto::getCategoria)    // [3] referencia a método
                .distinct()
                .count();

        return String.format(
            "Total: %d productos | Precio promedio: S/%.2f | " +
            "Valor inventario: S/%.2f | Categorías: %d | Stock bajo: %d",
            total, promedio, valorTotal, categorias, stockBajo
        );
    }
}
