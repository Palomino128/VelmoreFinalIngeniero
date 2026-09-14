package pe.edu.velmore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.velmore.dto.BusquedaProductoDto;
import pe.edu.velmore.dto.ContactoDto;
import pe.edu.velmore.model.Categoria;
import pe.edu.velmore.model.Producto;
import pe.edu.velmore.monitor.VelmoreMicrometerMetrics;
import pe.edu.velmore.service.ContactoService;
import pe.edu.velmore.service.ProductoService;
import pe.edu.velmore.util.ProductoAnalisis;

import java.util.List;

@Controller
public class CatalogoController {

    private static final Logger log = LoggerFactory.getLogger(CatalogoController.class);

    private final ProductoService   productoService;
    private final ContactoService   contactoService;
    private final VelmoreMicrometerMetrics metricas;

    // ── [UTIL] Inyección del analizador de catálogo ──────────────
    private final ProductoAnalisis  productoAnalisis;

    public CatalogoController(ProductoService productoService,
                              ContactoService contactoService,
                              VelmoreMicrometerMetrics metricas,
                              ProductoAnalisis productoAnalisis) {
        this.productoService   = productoService;
        this.contactoService   = contactoService;
        this.metricas          = metricas;
        this.productoAnalisis  = productoAnalisis;
    }

    // ─────────────────────────────────────────────────────────────
    // INICIO — muestra 4 productos + estadísticas de análisis
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/")
    public String inicio(Model model) {
        List<Producto> activos = productoService.listarActivos();

        // [UTIL — MÉTODO DE LENGUAJE] limit() sobre stream para la portada
        model.addAttribute("productos",       activos.stream().limit(4).toList());
        model.addAttribute("totalProductos",  productoService.totalProductos());
        model.addAttribute("totalCategorias", productoService.totalCategorias());
        model.addAttribute("valorInventario", productoService.valorInventario());

        // ── [UTIL] ProductoAnalisis en acción ──────────────────────
        // [MÉTODO] calcularPrecioPromedio → retorna primitivo double
        double promedio = productoAnalisis.calcularPrecioPromedio(activos);
        model.addAttribute("precioPromedio", String.format("%.2f", promedio));

        // [MÉTODO] contarStockBajo → retorna primitivo int
        int stockBajo = productoAnalisis.contarStockBajo(activos);
        model.addAttribute("alertaStockBajo", stockBajo);

        // [MÉTODO] generarResumen → String con estadísticas completas
        String resumen = productoAnalisis.generarResumen(activos);
        log.info("[INICIO] {}", resumen);

        return "inicio";
    }

    // ─────────────────────────────────────────────────────────────
    // CATÁLOGO — búsqueda con resumen de análisis
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/catalogo")
    public String catalogo(@ModelAttribute BusquedaProductoDto filtro, Model model) {
        log.info("Búsqueda de catálogo ejecutada con filtro: {}", filtro);
        metricas.registrarBusqueda(); // [MÉTRICA] velmore.busquedas.total++

        List<Producto> resultados = productoService.buscar(filtro);
        model.addAttribute("productos",  resultados);
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("filtro",     filtro);

        // [UTIL — MÉTODO] calcularPrecioPromedio sobre los resultados filtrados
        model.addAttribute("precioPromedioFiltrado",
                String.format("%.2f", productoAnalisis.calcularPrecioPromedio(resultados)));

        // [UTIL — MÉTODO] filtrarEconomicos → usa lambdas y Stream internamente
        model.addAttribute("economicos", productoAnalisis.filtrarEconomicos(resultados));

        return "catalogo";
    }

    // ─────────────────────────────────────────────────────────────
    // DESTACADOS — top 6 + producto más caro + análisis
    // ─────────────────────────────────────────────────────────────
    /** feat: endpoint de productos destacados (top 6 activos) */
    @GetMapping("/catalogo/destacados")
    public String destacados(Model model) {
        log.info("Cargando productos destacados");
        List<Producto> activos = productoService.listarActivos();

        // [UTIL — MÉTODO DE LENGUAJE] limit(6) sobre stream
        model.addAttribute("productos",  activos.stream().limit(6).toList());
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("filtro",     new BusquedaProductoDto());

        // [UTIL — MÉTODO] productoMasCaro → usa stream().max() internamente
        productoAnalisis.productoMasCaro(activos)
                .ifPresent(p -> model.addAttribute("productoPremium", p));

        // [UTIL — MÉTODO] productoMenosStock → usa stream().min() internamente
        productoAnalisis.productoMenosStock(activos)
                .ifPresent(p -> model.addAttribute("alertaStock", p));

        return "catalogo";
    }

    // ─────────────────────────────────────────────────────────────
    // DETALLE — ficha de producto con indicador de stock
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        metricas.registrarVistaDetalle(); // [MÉTRICA] velmore.detalle.visitas++
        Producto producto = productoService.obtenerPorId(id);
        model.addAttribute("producto", producto);

        // [UTIL — MÉTODO] tieneStockSuficiente → retorna primitivo boolean
        boolean hayStock = productoAnalisis.tieneStockSuficiente(producto);
        model.addAttribute("hayStock", hayStock);

        // [UTIL — MÉTODO] aplicarDescuento → retorna primitivo double (10% demo)
        double precioOferta = productoAnalisis.aplicarDescuento(producto.getPrecio(), 0.10);
        model.addAttribute("precioOferta", String.format("%.2f", precioOferta));

        return "detalle";
    }

    // ─────────────────────────────────────────────────────────────
    // CONTACTO
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/contacto/{id}")
    public String contacto(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(id));
        model.addAttribute("contacto", new ContactoDto());
        return "contacto";
    }

    @PostMapping("/contacto/mensaje")
    public String generarMensaje(@ModelAttribute ContactoDto contacto, Model model) {
        metricas.registrarOrdenWhatsApp(); // [MÉTRICA] velmore.ordenes.whatsapp++
        model.addAttribute("producto",      productoService.obtenerPorId(contacto.getProductoId()));
        model.addAttribute("mensaje",       contactoService.generarMensaje(contacto));
        model.addAttribute("whatsappLink",  contactoService.generarLinkWhatsApp(contacto));
        return "contacto";
    }
}
