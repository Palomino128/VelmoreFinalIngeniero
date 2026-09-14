package pe.edu.velmore.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de control de intentos de login fallidos.
 *
 * RAMA: feature/autenticacion-mejorada
 *
 * Funcionalidad:
 *  - Registra intentos fallidos por usuario
 *  - Bloquea la cuenta tras MAX_INTENTOS_LOGIN fallos
 *  - Desbloquea automáticamente tras MINUTOS_BLOQUEO minutos
 */
@Component
public class LoginIntentoService {

    private static final Logger log = LoggerFactory.getLogger(LoginIntentoService.class);

    // Mapa: email → número de intentos fallidos
    private final Map<String, Integer>       intentosFallidos = new HashMap<>();
    // Mapa: email → momento del bloqueo
    private final Map<String, LocalDateTime> tiempoBloqueo    = new HashMap<>();

    /**
     * Registra un intento de login fallido.
     * Si supera el máximo, bloquea la cuenta.
     *
     * @param email correo del usuario que falló
     */
    public void registrarFallo(String email) {
        // [PRIMITIVO] int para contar intentos
        int intentos = intentosFallidos.getOrDefault(email, 0) + 1;
        intentosFallidos.put(email, intentos);

        if (intentos >= SecurityConstants.MAX_INTENTOS_LOGIN) {
            tiempoBloqueo.put(email, LocalDateTime.now());
            log.warn("[SEGURIDAD] Cuenta bloqueada: {} tras {} intentos fallidos", email, intentos);
        } else {
            log.info("[SEGURIDAD] Intento fallido {}/{} para: {}", intentos, SecurityConstants.MAX_INTENTOS_LOGIN, email);
        }
    }

    /**
     * Verifica si una cuenta está bloqueada.
     *
     * @param  email correo a verificar
     * @return true si está bloqueada, false si puede intentar login
     */
    public boolean estaBloqueada(String email) {
        LocalDateTime bloqueadoEn = tiempoBloqueo.get(email);
        if (bloqueadoEn == null) return false;

        // Si ya pasaron los minutos de bloqueo → desbloquear
        if (LocalDateTime.now().isAfter(bloqueadoEn.plusMinutes(SecurityConstants.MINUTOS_BLOQUEO))) {
            resetear(email);
            log.info("[SEGURIDAD] Cuenta desbloqueada automáticamente: {}", email);
            return false;
        }
        return true;
    }

    /**
     * Resetea los contadores tras un login exitoso.
     *
     * @param email correo del usuario que ingresó correctamente
     */
    public void resetear(String email) {
        intentosFallidos.remove(email);
        tiempoBloqueo.remove(email);
    }

    /**
     * Retorna los intentos fallidos actuales de un usuario.
     * Retorna primitivo int.
     */
    public int getIntentos(String email) {
        return intentosFallidos.getOrDefault(email, 0);
    }
}
