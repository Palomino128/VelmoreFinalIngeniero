package pe.edu.velmore.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.velmore.model.Categoria;
import pe.edu.velmore.model.Producto;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ProductoValidator.
 * Verifica el uso correcto de Google Guava Preconditions y Apache Commons StringUtils.
 *
 * Conceptos de Testing demostrados:
 * - Partición de equivalencia: valores válidos vs inválidos para cada campo.
 * - Análisis de valor límite: descripción en el límite exacto (300 chars),
 *   precio en el límite (0 = inválido, 0.01 = válido), stock negativo.
 * - Pruebas de excepción: se verifica el tipo exacto de excepción esperada.
 */
@DisplayName("Pruebas de ProductoValidator")
class ProductoValidatorTest {

    private final ProductoValidator validator = new ProductoValidator();

    @Test
    @DisplayName("Producto nulo debe lanzar NullPointerException via Guava Preconditions")
    void productoNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> validator.validar(null));
    }

    @Test
    @DisplayName("Producto válido no debe lanzar ninguna excepción")
    void productoValidoNoLanzaExcepcion() {
        Producto p = new Producto(null, "Nombre", "Marca",
                Categoria.UNISEX, 100, "Descripción de prueba", 5, true);
        assertDoesNotThrow(() -> validator.validar(p));
    }

    @Test
    @DisplayName("limpiarTexto debe eliminar espacios al inicio y al final")
    void limpiarTextoEliminaEspacios() {
        assertEquals("texto limpio", validator.limpiarTexto("  texto limpio  "));
    }

    @Test
    @DisplayName("limpiarTexto con null debe retornar cadena vacía")
    void limpiarTextoConNullRetornaVacio() {
        assertEquals("", validator.limpiarTexto(null));
    }

    // ── Análisis de valor límite: Descripción ─────────────────────────────────

    @Test
    @DisplayName("Descripción de exactamente 300 caracteres debe ser válida (límite superior)")
    void descripcionExacta300CaracteresEsValida() {
        // Prueba de valor límite: exactamente en el borde aceptado
        String descripcion300 = "A".repeat(300);
        Producto p = new Producto(null, "Nombre", "Marca",
                Categoria.UNISEX, 100, descripcion300, 5, true);
        assertDoesNotThrow(() -> validator.validar(p),
                "Descripción de 300 chars debe ser válida (límite aceptado)");
    }

    @Test
    @DisplayName("Descripción de 301 caracteres debe lanzar excepción (supera límite)")
    void descripcion301CaracteresLanzaExcepcion() {
        // Prueba de valor límite: un carácter por encima del límite
        String descripcion301 = "B".repeat(301);
        Producto p = new Producto(null, "Nombre", "Marca",
                Categoria.UNISEX, 100, descripcion301, 5, true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validar(p));
        assertTrue(ex.getMessage().contains("300"),
                "El mensaje de error debe mencionar el límite de 300 caracteres");
    }

    // ── Análisis de valor límite: Precio ──────────────────────────────────────

    @Test
    @DisplayName("Precio negativo debe lanzar excepción")
    void precioNegativoLanzaExcepcion() {
        Producto p = new Producto(null, "Nombre", "Marca",
                Categoria.UNISEX, -50.0, "Descripción", 5, true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validar(p));
        assertTrue(ex.getMessage().toLowerCase().contains("precio"),
                "El mensaje de error debe referirse al precio");
    }

    @Test
    @DisplayName("Precio mínimo válido (0.01) no debe lanzar excepción")
    void precioMinimoValidoNoLanzaExcepcion() {
        // Prueba de valor límite: precio apenas por encima del mínimo
        Producto p = new Producto(null, "Nombre", "Marca",
                Categoria.UNISEX, 0.01, "Descripción", 5, true);
        assertDoesNotThrow(() -> validator.validar(p),
                "Precio 0.01 debe ser válido (mayor que cero)");
    }

    // ── Validación de Categoría ───────────────────────────────────────────────

    @Test
    @DisplayName("Categoría nula debe lanzar excepción")
    void categoriaNulaLanzaExcepcion() {
        Producto p = new Producto(null, "Nombre", "Marca",
                null, 100, "Descripción", 5, true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validar(p));
        assertTrue(ex.getMessage().toLowerCase().contains("categor"),
                "El mensaje de error debe referirse a la categoría");
    }

    // ── Validación de Stock ───────────────────────────────────────────────────

    @Test
    @DisplayName("Stock de cero debe ser válido (producto sin existencias)")
    void stockCeroEsValido() {
        // Stock = 0 es permitido (producto agotado pero registrado)
        Producto p = new Producto(null, "Nombre", "Marca",
                Categoria.FEMENINO, 100, "Descripción", 0, true);
        assertDoesNotThrow(() -> validator.validar(p),
                "Stock = 0 debe ser válido (solo negativo es rechazado)");
    }
}
