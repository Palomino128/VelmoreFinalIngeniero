package pe.edu.velmore.service;

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.velmore.dao.InMemoryProductoDao;
import pe.edu.velmore.dto.BusquedaProductoDto;
import pe.edu.velmore.model.Categoria;
import pe.edu.velmore.model.Producto;
import pe.edu.velmore.validation.ProductoValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ProductoServiceImpl.
 * Cobertura: búsqueda, registro, actualización, eliminación, validaciones y estadísticas.
 */
@DisplayName("Pruebas de ProductoServiceImpl")
class ProductoServiceImplTest {

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoServiceImpl(new InMemoryProductoDao(), new ProductoValidator());
    }

    // ── Búsqueda ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Buscar por nombre debe retornar productos coincidentes")
    void buscarPorNombreDebeRetornarCoincidencias() {
        BusquedaProductoDto filtro = new BusquedaProductoDto();
        filtro.setTexto("Khamrah");
        List<Producto> resultado = service.buscar(filtro);
        assertFalse(resultado.isEmpty(), "Debe encontrar al menos un producto con 'Khamrah'");
        assertTrue(resultado.stream().allMatch(p ->
                p.getNombre().toLowerCase().contains("khamrah")));
    }

    @Test
    @DisplayName("Buscar por categoría MASCULINO debe filtrar correctamente")
    void buscarPorCategoriaMasculino() {
        BusquedaProductoDto filtro = new BusquedaProductoDto();
        filtro.setCategoria(Categoria.MASCULINO);
        List<Producto> resultado = service.buscar(filtro);
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(p -> p.getCategoria() == Categoria.MASCULINO));
    }

    @Test
    @DisplayName("Buscar sin filtro debe retornar todos los productos activos")
    void buscarSinFiltroRetornaTodos() {
        List<Producto> todos = service.buscar(null);
        assertEquals(service.totalProductos(), todos.size());
    }

    // ── Listar activos ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Listar activos debe retornar solo productos activos")
    void listarActivosRetornaSoloActivos() {
        List<Producto> activos = service.listarActivos();
        assertTrue(activos.stream().allMatch(Producto::isActivo),
                "Todos los productos deben estar activos");
    }

    @Test
    @DisplayName("Listar activos debe retornar productos ordenados por nombre")
    void listarActivosOrdenadosPorNombre() {
        List<Producto> activos = service.listarActivos();
        for (int i = 0; i < activos.size() - 1; i++) {
            assertTrue(activos.get(i).getNombre()
                    .compareTo(activos.get(i + 1).getNombre()) <= 0,
                    "Los productos deben estar ordenados alfabéticamente");
        }
    }

    // ── Registrar ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Registrar producto válido debe incrementar el total")
    void registrarProductoValidoIncrementaTotal() {
        int totalAntes = service.totalProductos();
        Producto nuevo = new Producto(null, "Oud Rose", "Swiss Arabian",
                Categoria.UNISEX, 199.0, "Fragancia de oud y rosa.", 5, true);
        service.registrar(nuevo);
        assertEquals(totalAntes + 1, service.totalProductos());
    }

    @Test
    @DisplayName("Registrar con precio cero debe lanzar excepción")
    void registrarConPrecioCeroLanzaExcepcion() {
        Producto producto = new Producto(null, "Prueba", "Marca",
                Categoria.UNISEX, 0, "Descripción", 1, true);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(producto));
    }

    @Test
    @DisplayName("Registrar con nombre vacío debe lanzar excepción")
    void registrarConNombreVacioLanzaExcepcion() {
        Producto producto = new Producto(null, "   ", "Marca",
                Categoria.UNISEX, 100, "Descripción", 1, true);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(producto));
    }

    @Test
    @DisplayName("Registrar con marca vacía debe lanzar excepción")
    void registrarConMarcaVaciaLanzaExcepcion() {
        Producto producto = new Producto(null, "Nombre", "",
                Categoria.UNISEX, 100, "Descripción", 1, true);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(producto));
    }

    @Test
    @DisplayName("Registrar con stock negativo debe lanzar excepción")
    void registrarConStockNegativoLanzaExcepcion() {
        Producto producto = new Producto(null, "Nombre", "Marca",
                Categoria.UNISEX, 100, "Descripción", -1, true);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(producto));
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Actualizar producto existente debe reflejar cambios")
    void actualizarProductoExistenteRefleja() {
        Producto actualizado = new Producto(1L, "Khamrah Editado", "Lattafa",
                Categoria.UNISEX, 180.0, "Editado.", 15, true);
        service.actualizar(1L, actualizado);
        Producto resultado = service.obtenerPorId(1L);
        assertEquals("Khamrah Editado", resultado.getNombre());
        assertEquals(15, resultado.getStock());
    }

    @Test
    @DisplayName("Actualizar producto inexistente debe lanzar excepción")
    void actualizarProductoInexistenteLanzaExcepcion() {
        Producto producto = new Producto(999L, "X", "Y",
                Categoria.UNISEX, 100, "desc", 1, true);
        // Guava LoadingCache envuelve la excepción en UncheckedExecutionException
        try {
            service.actualizar(999L, producto);
            fail("Se esperaba una excepción para producto inexistente");
        } catch (IllegalArgumentException | UncheckedExecutionException ex) {
            // Ambas son aceptables: excepción directa o envuelta por Guava
            assertTrue(true, "Excepción correctamente lanzada para ID inexistente");
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Eliminar producto debe reducir el total")
    void eliminarProductoReduceTotal() {
        int totalAntes = service.totalProductos();
        service.eliminar(1L);
        assertEquals(totalAntes - 1, service.totalProductos());
    }

    @Test
    @DisplayName("Obtener por ID inexistente debe lanzar excepción")
    void obtenerPorIdInexistenteLanzaExcepcion() {
        // Guava LoadingCache envuelve la excepción en UncheckedExecutionException
        try {
            service.obtenerPorId(9999L);
            fail("Se esperaba una excepción para ID inexistente");
        } catch (IllegalArgumentException | UncheckedExecutionException ex) {
            // Ambas son aceptables: excepción directa o envuelta por Guava
            assertTrue(true, "Excepción correctamente lanzada para ID 9999");
        }
    }

    // ── Estadísticas ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valor de inventario debe ser mayor a cero con productos iniciales")
    void valorInventarioMayorACero() {
        assertTrue(service.valorInventario() > 0,
                "El valor del inventario debe ser positivo");
    }

    @Test
    @DisplayName("Total de categorías no debe exceder el número de valores del enum")
    void totalCategoriasNoExcedeEnum() {
        long total = service.totalCategorias();
        assertTrue(total >= 1 && total <= Categoria.values().length);
    }
}
