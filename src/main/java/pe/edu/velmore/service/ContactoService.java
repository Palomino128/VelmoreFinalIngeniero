package pe.edu.velmore.service;

import pe.edu.velmore.dto.ContactoDto;

public interface ContactoService {
    String generarMensaje(ContactoDto dto);
    String generarLinkWhatsApp(ContactoDto dto);
}
