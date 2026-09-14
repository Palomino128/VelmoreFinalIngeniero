package pe.edu.velmore.controller;

import pe.edu.velmore.dto.CarritoItemDto;
import pe.edu.velmore.service.CarritoService;
import pe.edu.velmore.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CarritoController {
    private final ProductoService productoService;
    private final CarritoService carritoService;

    public CarritoController(ProductoService productoService, CarritoService carritoService) {
        this.productoService = productoService;
        this.carritoService = carritoService;
    }

    @GetMapping("/carrito")
    public String carrito(Model model) {
        model.addAttribute("productos", productoService.listarActivos());
        return "carrito";
    }

    @PostMapping("/carrito/whatsapp")
    public String contactarWhatsApp(@RequestParam(value = "productoId", required = false) List<Long> productoIds,
                                    @RequestParam(value = "cantidad", required = false) List<Integer> cantidades,
                                    @RequestParam(value = "nombreCliente", required = false) String nombreCliente,
                                    Model model) {
        List<CarritoItemDto> items = carritoService.normalizar(productoIds, cantidades);

        model.addAttribute("productos", productoService.listarActivos());
        model.addAttribute("mensaje", carritoService.generarMensaje(items, nombreCliente));
        model.addAttribute("whatsappLink", carritoService.generarLinkWhatsApp(items, nombreCliente));
        model.addAttribute("nombreCliente", nombreCliente);

        if (items.isEmpty()) {
            model.addAttribute("error", "Selecciona al menos un producto para contactar por WhatsApp.");
        }

        return "carrito";
    }
}
