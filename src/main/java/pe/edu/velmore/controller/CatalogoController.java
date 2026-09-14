package pe.edu.velmore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.velmore.dto.BusquedaProductoDto;
import pe.edu.velmore.dto.ContactoDto;
import pe.edu.velmore.model.Categoria;
import pe.edu.velmore.monitor.VelmoreMicrometerMetrics;
import pe.edu.velmore.service.ContactoService;
import pe.edu.velmore.service.ProductoService;

@Controller
public class CatalogoController {
    private static final Logger log = LoggerFactory.getLogger(CatalogoController.class);
    private final ProductoService productoService;
    private final ContactoService contactoService;
    // CAPA 3 — Métricas de negocio Micrometer
    private final VelmoreMicrometerMetrics metricas;

    public CatalogoController(ProductoService productoService,
                              ContactoService contactoService,
                              VelmoreMicrometerMetrics metricas) {
        this.productoService = productoService;
        this.contactoService = contactoService;
        this.metricas        = metricas;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("productos", productoService.listarActivos().stream().limit(4).toList());
        model.addAttribute("totalProductos", productoService.totalProductos());
        model.addAttribute("totalCategorias", productoService.totalCategorias());
        model.addAttribute("valorInventario", productoService.valorInventario());
        return "inicio";
    }

    @GetMapping("/catalogo")
    public String catalogo(@ModelAttribute BusquedaProductoDto filtro, Model model) {
        log.info("Búsqueda de catálogo ejecutada con filtro: {}", filtro);
        metricas.registrarBusqueda(); // [MÉTRICA] velmore.busquedas.total++
        model.addAttribute("productos", productoService.buscar(filtro));
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("filtro", filtro);
        return "catalogo";
    }

    /** feat: endpoint de productos destacados (top 6 activos) */
    @GetMapping("/catalogo/destacados")
    public String destacados(Model model) {
        log.info("Cargando productos destacados");
        model.addAttribute("productos", productoService.listarActivos().stream().limit(6).toList());
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("filtro", new BusquedaProductoDto());
        return "catalogo";
    }

    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        metricas.registrarVistaDetalle(); // [MÉTRICA] velmore.detalle.visitas++
        model.addAttribute("producto", productoService.obtenerPorId(id));
        return "detalle";
    }

    @GetMapping("/contacto/{id}")
    public String contacto(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(id));
        model.addAttribute("contacto", new ContactoDto());
        return "contacto";
    }

    @PostMapping("/contacto/mensaje")
    public String generarMensaje(@ModelAttribute ContactoDto contacto, Model model) {
        metricas.registrarOrdenWhatsApp(); // [MÉTRICA] velmore.ordenes.whatsapp++
        model.addAttribute("producto", productoService.obtenerPorId(contacto.getProductoId()));
        model.addAttribute("mensaje", contactoService.generarMensaje(contacto));
        model.addAttribute("whatsappLink", contactoService.generarLinkWhatsApp(contacto));
        return "contacto";
    }
}

