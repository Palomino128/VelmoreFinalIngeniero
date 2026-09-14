package pe.edu.velmore.security;

/**
 * Constantes de seguridad del sistema VELMORE.
 *
 * RAMA: feature/mejora-catalogo
 * Cambios: se ajusta MAX_INTENTOS_LOGIN a 5 y se agrega PRECIO_MAXIMO_CATALOGO.
 *
 * ⚠️  CONFLICTO INTENCIONAL con feature/autenticacion-mejorada:
 *     Ambas ramas modifican MAX_INTENTOS_LOGIN con valores distintos (3 vs 5).
 *     Esto demuestra cómo Git detecta y notifica un merge conflict.
 */
public class SecurityConstants {

    private SecurityConstants() {}

    /** Clave de sesión para el usuario autenticado */
    public static final String USER_SESSION = "USER_SESSION";

    // ── Constante modificada en esta rama (valor distinto → conflicto) ──
    /** Máximo de intentos de login (feature/mejora-catalogo propone 5) */
    public static final int MAX_INTENTOS_LOGIN = 5;

    /** Precio máximo permitido para registrar un producto en el catálogo */
    public static final double PRECIO_MAXIMO_CATALOGO = 999.99;

    /** Longitud máxima del nombre de producto */
    public static final int MAX_LONGITUD_NOMBRE = 150;
}
