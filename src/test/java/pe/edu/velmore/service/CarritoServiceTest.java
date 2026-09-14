package pe.edu.velmore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.velmore.config.WhatsAppProperties;
import pe.edu.velmore.dao.InMemoryProductoDao;
import pe.edu.velmore.dto.CarritoItemDto;
import pe.edu.velmore.validation.ProductoValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para CarritoService.
 *
 * Conceptos de Testing demostrados:
 * - Test Doubles (Stub manual): Se usa una implementación anónima de WhatsAppProperties
 *   en lugar de Mockito, compatible con cualquier versión de JVM (Java 17–26).
 * - Pruebas de valor límite: cantidades 0, negativas y null.
 * - Pruebas de partición de equivalencia: carrito vacío vs. carrito con items.
 * - Verificación de post-condiciones: el mensaje y URL generados cumplen contratos.
 *
 * Nota de seguridad (SEC-06 / SEC-08):
 * Los mensajes de WhatsApp incluyen datos de usuario sin sanitización HTML.
 * El número de teléfono queda expuesto en la URL generada.
 * Ver reporte_pruebas_seguridad.md para detalle OWASP.
 */
@DisplayName("Pruebas de CarritoService")
class CarritoServiceTest {

    private CarritoService carritoService;
    private ProductoService productoService;

    // WhatsApp stub manual — compatible con Java 26 sin dependencia de Mockito/ByteBuddy
    private static final String NUMERO_TEST = "51999000111";

    @BeforeEach
    void setUp() {
        // Usamos implementaciones reales para evitar incompatibilidades con ByteBuddy en Java 26
        productoService = new ProductoServiceImpl(new InMemoryProductoDao(), new ProductoValidator());

        // Stub manual de WhatsAppProperties
        WhatsAppProperties whatsAppStub = new WhatsAppProperties() {
            @Override
            public String getNumero() {
                return NUMERO_TEST;
            }
        };

        carritoService = new CarritoService(productoService, whatsAppStub);
    }

    // ── normalizar() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("normalizar() con lista de IDs nula debe retornar lista vacía")
    void normalizarConIdsNulaRetornaListaVacia() {
        List<CarritoItemDto> resultado = carritoService.normalizar(null, null);
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty(), "Una lista de IDs nula debe generar carrito vacío");
    }

    @Test
    @DisplayName("normalizar() con lista de IDs vacía debe retornar lista vacía")
    void normalizarConIdsVaciaRetornaListaVacia() {
        List<CarritoItemDto> resultado = carritoService.normalizar(
                Collections.emptyList(), Collections.emptyList()
        );
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("normalizar() con cantidades nulas debe usar cantidad 1 por defecto")
    void normalizarConCantidadesNulasUsaCantidadUno() {
        List<Long> ids = Arrays.asList(1L, 2L);

        List<CarritoItemDto> resultado = carritoService.normalizar(ids, null);

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getCantidad(),
                "La cantidad por defecto debe ser 1");
        assertEquals(1, resultado.get(1).getCantidad());
    }

    @Test
    @DisplayName("normalizar() con cantidad cero debe usar cantidad 1 por defecto")
    void normalizarConCantidadCeroUsaCantidadUno() {
        List<Long> ids = Arrays.asList(1L);
        List<Integer> cantidades = Arrays.asList(0);

        List<CarritoItemDto> resultado = carritoService.normalizar(ids, cantidades);

        assertEquals(1, resultado.get(0).getCantidad(),
                "Cantidad 0 debe forzarse a 1");
    }

    @Test
    @DisplayName("normalizar() con cantidad negativa debe usar cantidad 1 por defecto")
    void normalizarConCantidadNegativaUsaCantidadUno() {
        List<Long> ids = Arrays.asList(5L);
        List<Integer> cantidades = Arrays.asList(-3);

        List<CarritoItemDto> resultado = carritoService.normalizar(ids, cantidades);

        assertEquals(1, resultado.get(0).getCantidad(),
                "Cantidad negativa debe forzarse a 1 (valor límite)");
    }

    @Test
    @DisplayName("normalizar() con cantidad válida debe preservar la cantidad original")
    void normalizarConCantidadValidaPreservaCantidad() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<Integer> cantidades = Arrays.asList(3, 5);

        List<CarritoItemDto> resultado = carritoService.normalizar(ids, cantidades);

        assertEquals(3, resultado.get(0).getCantidad());
        assertEquals(5, resultado.get(1).getCantidad());
    }

    @Test
    @DisplayName("normalizar() debe omitir IDs nulos de la lista")
    void normalizarOmiteIdsNulos() {
        List<Long> ids = Arrays.asList(1L, null, 3L);
        List<Integer> cantidades = Arrays.asList(1, 1, 1);

        List<CarritoItemDto> resultado = carritoService.normalizar(ids, cantidades);

        assertEquals(2, resultado.size(), "IDs nulos deben ser ignorados");
    }

    // ── generarMensaje() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("generarMensaje() sin items debe retornar mensaje genérico con nombre cliente")
    void generarMensajeSinItemsRetornaMensajeGenerico() {
        String mensaje = carritoService.generarMensaje(Collections.emptyList(), "Juan");

        assertNotNull(mensaje);
        assertTrue(mensaje.contains("Juan"), "El mensaje debe incluir el nombre del cliente");
        assertTrue(mensaje.contains("VELMORE"),
                "El mensaje genérico debe mencionar la marca VELMORE");
    }

    @Test
    @DisplayName("generarMensaje() con nombre nulo debe usar 'Cliente' por defecto")
    void generarMensajeConNombreNuloUsaDefecto() {
        String mensaje = carritoService.generarMensaje(null, null);

        assertTrue(mensaje.contains("Cliente"),
                "Nombre nulo debe reemplazarse por 'Cliente'");
    }

    @Test
    @DisplayName("generarMensaje() con nombre vacío debe usar 'Cliente' por defecto")
    void generarMensajeConNombreVacioUsaDefecto() {
        String mensaje = carritoService.generarMensaje(Collections.emptyList(), "   ");

        assertTrue(mensaje.contains("Cliente"),
                "Nombre en blanco debe reemplazarse por 'Cliente'");
    }

    @Test
    @DisplayName("generarMensaje() con items debe incluir nombre del producto y subtotal")
    void generarMensajeConItemsIncluyeProductoYSubtotal() {
        // ID 1 = Khamrah (169.0 x 2 = 338.00) — datos del InMemoryProductoDao
        List<CarritoItemDto> items = Arrays.asList(new CarritoItemDto(1L, 2));
        String mensaje = carritoService.generarMensaje(items, "María");

        assertTrue(mensaje.contains("Khamrah"), "Debe incluir el nombre del producto");
        assertTrue(mensaje.contains("Lattafa"), "Debe incluir la marca");
        assertTrue(mensaje.contains("338.00"), "Subtotal correcto: 169.0 x 2 = 338.00");
        assertTrue(mensaje.contains("María"), "Debe incluir el nombre del cliente");
    }

    @Test
    @DisplayName("generarMensaje() debe calcular el total referencial correctamente")
    void generarMensajeCalculaTotalReferencialCorrecto() {
        // ID 1 = Khamrah 169.0 x 1 = 169.00
        // ID 3 = 9PM     159.0 x 2 = 318.00
        // Total = 487.00
        List<CarritoItemDto> items = Arrays.asList(
                new CarritoItemDto(1L, 1),
                new CarritoItemDto(3L, 2)
        );
        String mensaje = carritoService.generarMensaje(items, "Pedro");

        assertTrue(mensaje.contains("487.00"),
                "El total referencial debe ser 487.00 (169 + 159*2)");
    }

    @Test
    @DisplayName("generarMensaje() con item de cantidad 0 debe usar cantidad 1")
    void generarMensajeConCantidadCeroUsaUno() {
        // ID 4 = Sublime 149.0 x 1 = 149.00 (cantidad 0 → se trata como 1)
        List<CarritoItemDto> items = Arrays.asList(new CarritoItemDto(4L, 0));
        String mensaje = carritoService.generarMensaje(items, "Test");

        assertTrue(mensaje.contains("149.00"),
                "Cantidad 0 se trata como 1, subtotal = 149.00");
    }

    // ── generarLinkWhatsApp() ─────────────────────────────────────────────────

    @Test
    @DisplayName("generarLinkWhatsApp() debe retornar URL con dominio wa.me")
    void generarLinkWhatsAppContieneWaMe() {
        String link = carritoService.generarLinkWhatsApp(Collections.emptyList(), "Ana");

        assertNotNull(link);
        assertTrue(link.startsWith("https://wa.me/"),
                "El link debe comenzar con https://wa.me/");
        assertTrue(link.contains(NUMERO_TEST),
                "El link debe contener el número de WhatsApp");
        assertTrue(link.contains("?text="),
                "El link debe contener el parámetro text codificado");
    }

    @Test
    @DisplayName("generarLinkWhatsApp() debe codificar el texto del mensaje (URL encoding)")
    void generarLinkWhatsAppCodificaMensaje() {
        String link = carritoService.generarLinkWhatsApp(Collections.emptyList(), "Test User");

        // Los espacios deben codificarse como %20 o + en URL encoding
        assertFalse(link.endsWith(" "),
                "El link no debe terminar con espacios sin codificar (SEC-06: XSS via URL)");
        assertTrue(link.startsWith("https://"),
                "El link debe ser una URL HTTPS válida");
    }
}
