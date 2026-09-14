package pe.edu.velmore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppProperties {
    @Value("${velmore.whatsapp.numero:51999999999}")
    private String numero;

    public String getNumero() {
        return numero;
    }
}
