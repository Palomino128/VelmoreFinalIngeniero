package pe.edu.velmore.security;

/**
 * Constantes de seguridad del sistema VELMORE.
 *
 * ── RESOLUCIÓN DE CONFLICTO ──────────────────────────────────────
 * Conflicto entre:
 *   feature/autenticacion-mejorada → MAX_INTENTOS_LOGIN = 3
 *   feature/mejora-catalogo        → MAX_INTENTOS_LOGIN = 5
 *
 * Decisión: se mantiene 3 intentos (política de seguridad estricta)
 *           y se incorporan las constantes de catálogo de ambas ramas.
 * ─────────────────────────────────────────────────────────────────
 */
public class SecurityConstants {

    private SecurityConstants() {}

    /** Clave de sesión para el usuario autenticado */
    public static final String USER_SESSION = "USER_SESSION";

    // ── Autenticación (feature/autenticacion-mejorada) ──────────────
    /** Máximo de intentos de login — RESUELTO: valor más seguro = 3 */
    public static final int MAX_INTENTOS_LOGIN = 3;

    /** Tiempo de bloqueo en minutos tras superar MAX_INTENTOS_LOGIN */
    public static final int MINUTOS_BLOQUEO = 10;

    /** Duración de sesión activa en minutos */
    public static final int DURACION_SESION_MIN = 30;

    // ── Catálogo (feature/mejora-catalogo) ──────────────────────────
    /** Precio máximo permitido para registrar un producto */
    public static final double PRECIO_MAXIMO_CATALOGO = 999.99;

    /** Longitud máxima del nombre de producto */
    public static final int MAX_LONGITUD_NOMBRE = 150;
}
