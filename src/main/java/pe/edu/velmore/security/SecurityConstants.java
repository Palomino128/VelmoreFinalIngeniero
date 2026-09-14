package pe.edu.velmore.security;

/**
 * Constantes de seguridad del sistema VELMORE.
 *
 * RAMA: feature/autenticacion-mejorada
 * Cambios: se agregan límites de intentos de login y duración de sesión.
 */
public class SecurityConstants {

    private SecurityConstants() {}

    /** Clave de sesión para el usuario autenticado */
    public static final String USER_SESSION = "USER_SESSION";

    // ── Nuevas constantes de autenticación ──────────────────────────
    /** Máximo de intentos de login fallidos antes de bloquear (feature/autenticacion-mejorada) */
    public static final int MAX_INTENTOS_LOGIN = 3;

    /** Tiempo de bloqueo en minutos tras superar MAX_INTENTOS_LOGIN */
    public static final int MINUTOS_BLOQUEO = 10;

    /** Duración de sesión activa en minutos */
    public static final int DURACION_SESION_MIN = 30;
}
