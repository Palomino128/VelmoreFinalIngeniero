package pe.edu.velmore.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pe.edu.velmore.config.WhatsAppProperties;
import pe.edu.velmore.dto.ContactoDto;
import pe.edu.velmore.model.Producto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ContactoServiceImpl implements ContactoService {
    private final ProductoService productoService;
    private final WhatsAppProperties whatsAppProperties;

    public ContactoServiceImpl(ProductoService productoService, WhatsAppProperties whatsAppProperties) {
        this.productoService = productoService;
        this.whatsAppProperties = whatsAppProperties;
    }

    @Override
    public String generarMensaje(ContactoDto dto) {
        Producto producto = productoService.obtenerPorId(dto.getProductoId());
        String cliente = StringUtils.defaultIfBlank(dto.getNombreCliente(), "Cliente");
        int cantidad = dto.getCantidad() <= 0 ? 1 : dto.getCantidad();

        return "Hola, soy " + cliente + ". Estoy interesado en "
                + producto.getNombre() + " de " + producto.getMarca()
                + ". Cantidad solicitada: " + cantidad
                + ". ¿Me brindan más información?";
    }

    @Override
    public String generarLinkWhatsApp(ContactoDto dto) {
        String mensaje = generarMensaje(dto);
        String textoCodificado = URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
        return "https://wa.me/" + whatsAppProperties.getNumero() + "?text=" + textoCodificado;
    }
}
