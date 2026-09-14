package pe.edu.velmore.service;

import pe.edu.velmore.config.WhatsAppProperties;
import pe.edu.velmore.dto.CarritoItemDto;
import pe.edu.velmore.model.Producto;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarritoService {
    private final ProductoService productoService;
    private final WhatsAppProperties whatsAppProperties;

    public CarritoService(ProductoService productoService, WhatsAppProperties whatsAppProperties) {
        this.productoService = productoService;
        this.whatsAppProperties = whatsAppProperties;
    }

    public List<CarritoItemDto> normalizar(List<Long> productoIds, List<Integer> cantidades) {
        List<CarritoItemDto> items = new ArrayList<>();

        if (productoIds == null || productoIds.isEmpty()) {
            return items;
        }

        for (int i = 0; i < productoIds.size(); i++) {
            Long id = productoIds.get(i);
            int cantidad = 1;

            if (cantidades != null && i < cantidades.size() && cantidades.get(i) != null && cantidades.get(i) > 0) {
                cantidad = cantidades.get(i);
            }

            if (id != null) {
                items.add(new CarritoItemDto(id, cantidad));
            }
        }

        return items;
    }

    public String generarMensaje(List<CarritoItemDto> items, String nombreCliente) {
        String cliente = nombreCliente == null || nombreCliente.isBlank() ? "Cliente" : nombreCliente.trim();

        if (items == null || items.isEmpty()) {
            return "Hola, soy " + cliente + ". Deseo información sobre los perfumes VELMORE.";
        }

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Hola, soy ").append(cliente).append(". Estoy interesado en los siguientes productos:\n");

        double totalReferencial = 0;

        for (CarritoItemDto item : items) {
            Producto producto = productoService.obtenerPorId(item.getProductoId());
            int cantidad = item.getCantidad() <= 0 ? 1 : item.getCantidad();
            double subtotal = producto.getPrecio() * cantidad;
            totalReferencial += subtotal;

            mensaje.append("- ")
                    .append(producto.getNombre())
                    .append(" (")
                    .append(producto.getMarca())
                    .append(") x")
                    .append(cantidad)
                    .append(" - S/ ")
                    .append(String.format("%.2f", subtotal))
                    .append("\n");
        }

        mensaje.append("Total referencial: S/ ")
                .append(String.format("%.2f", totalReferencial))
                .append("\n¿Me brindan más información para realizar la compra?");

        return mensaje.toString();
    }

    public String generarLinkWhatsApp(List<CarritoItemDto> items, String nombreCliente) {
        String textoCodificado = URLEncoder.encode(generarMensaje(items, nombreCliente), StandardCharsets.UTF_8);
        return "https://wa.me/" + whatsAppProperties.getNumero() + "?text=" + textoCodificado;
    }
}
