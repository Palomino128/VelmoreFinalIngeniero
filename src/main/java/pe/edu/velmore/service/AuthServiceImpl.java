package pe.edu.velmore.service;

import pe.edu.velmore.dto.LoginDto;
import pe.edu.velmore.model.Usuario;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de autenticación VELMORE.
 *
 * <p>Las credenciales se leen desde {@code application.properties}
 * mediante {@code @Value}, permitiendo su personalización sin
 * modificar el código fuente. En producción se recomienda
 * sobrescribirlas con variables de entorno.</p>
 *
 * <h3>Propiedades utilizadas:</h3>
 * <ul>
 *   <li>{@code app.admin.usuario}   — usuario administrador (default: admin)</li>
 *   <li>{@code app.admin.clave}     — contraseña administrador (default: Velmore2024$)</li>
 *   <li>{@code app.cliente.usuario} — usuario cliente (default: cliente)</li>
 *   <li>{@code app.cliente.clave}   — contraseña cliente (default: cliente123)</li>
 * </ul>
 */
@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final String adminUsuario;
    private final String adminClave;
    private final String clienteUsuario;
    private final String clienteClave;

    /**
     * Constructor principal — Spring inyecta los valores desde application.properties.
     */
    public AuthServiceImpl(
            @Value("${app.admin.usuario:admin}") String adminUsuario,
            @Value("${app.admin.clave:Velmore2024$}") String adminClave,
            @Value("${app.cliente.usuario:cliente}") String clienteUsuario,
            @Value("${app.cliente.clave:cliente123}") String clienteClave) {
        this.adminUsuario   = adminUsuario;
        this.adminClave     = adminClave;
        this.clienteUsuario = clienteUsuario;
        this.clienteClave   = clienteClave;
    }

    @Override
    public Usuario autenticar(LoginDto login) {
        if (login == null) {
            log.warn("Intento de login vacío");
            return null;
        }

        String usuario = StringUtils.trimToEmpty(login.getUsuario());
        String clave = StringUtils.trimToEmpty(login.getClave());

        if (StringUtils.equals(usuario, adminUsuario) && StringUtils.equals(clave, adminClave)) {
            log.info("Acceso correcto como ADMIN");
            return new Usuario(adminUsuario, "", "Administrador VELMORE", "ADMIN");
        }

        if (StringUtils.equals(usuario, clienteUsuario) && StringUtils.equals(clave, clienteClave)) {
            log.info("Acceso correcto como CLIENTE");
            return new Usuario(clienteUsuario, "", "Cliente VELMORE", "CLIENTE");
        }

        log.warn("Credenciales incorrectas para usuario: {}", usuario);
        return null;
    }
}
