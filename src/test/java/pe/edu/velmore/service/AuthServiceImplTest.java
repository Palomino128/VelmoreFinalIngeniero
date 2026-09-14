package pe.edu.velmore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.velmore.dto.LoginDto;
import pe.edu.velmore.model.Usuario;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para AuthServiceImpl.
 *
 * Conceptos de Testing demostrados:
 * - Pruebas de caja negra: se evalúa el comportamiento externo del servicio
 *   sin depender de su implementación interna.
 * - Pruebas de valor límite: credenciales correctas, incorrectas, nulas y con espacios.
 * - Pruebas de regresión: garantizan que ningún cambio rompa la autenticación.
 *
 * Nota de diseño:
 * El servicio lee las credenciales desde application.properties (inyectadas via @Value).
 * En los tests se instancia directamente con los valores esperados, sin necesitar
 * contexto Spring, usando inyección por constructor (buena práctica).
 */
@DisplayName("Pruebas de AuthServiceImpl")
class AuthServiceImplTest {

    private AuthService authService;

    // Credenciales de prueba — coinciden con application.properties
    private static final String ADMIN_USUARIO = "admin";
    private static final String ADMIN_CLAVE   = "Velmore2024$";
    private static final String CLIENTE_USUARIO = "cliente";
    private static final String CLIENTE_CLAVE   = "cliente123";

    @BeforeEach
    void setUp() {
        // Constructor injection: mismos valores que application.properties
        authService = new AuthServiceImpl(ADMIN_USUARIO, ADMIN_CLAVE, CLIENTE_USUARIO, CLIENTE_CLAVE);
    }

    // ── Autenticación exitosa ─────────────────────────────────────────────────

    @Test
    @DisplayName("Login correcto como ADMIN debe retornar usuario con rol ADMIN")
    void loginAdminCorrectoRetornaUsuarioAdmin() {
        LoginDto login = new LoginDto();
        login.setUsuario("admin");
        login.setClave("Velmore2024$");

        Usuario resultado = authService.autenticar(login);

        assertNotNull(resultado, "El resultado no debe ser nulo para credenciales válidas");
        assertEquals("admin", resultado.getUsuario());
        assertEquals("ADMIN", resultado.getRol());
        assertTrue(resultado.esAdministrador(), "El usuario debe ser administrador");
    }

    @Test
    @DisplayName("Login correcto como CLIENTE debe retornar usuario con rol CLIENTE")
    void loginClienteCorrectoRetornaUsuarioCliente() {
        LoginDto login = new LoginDto();
        login.setUsuario("cliente");
        login.setClave("cliente123");

        Usuario resultado = authService.autenticar(login);

        assertNotNull(resultado, "El resultado no debe ser nulo para credenciales válidas");
        assertEquals("cliente", resultado.getUsuario());
        assertEquals("CLIENTE", resultado.getRol());
        assertTrue(resultado.esCliente(), "El usuario debe ser cliente");
        assertFalse(resultado.esAdministrador(), "El cliente no debe ser administrador");
    }

    // ── Credenciales incorrectas ──────────────────────────────────────────────

    @Test
    @DisplayName("Contraseña incorrecta debe retornar null")
    void loginConPasswordIncorrectoRetornaNull() {
        LoginDto login = new LoginDto();
        login.setUsuario("admin");
        login.setClave("password_malo");

        Usuario resultado = authService.autenticar(login);

        assertNull(resultado, "Debe retornar null con contraseña incorrecta");
    }

    @Test
    @DisplayName("Usuario inexistente debe retornar null")
    void loginConUsuarioInexistenteRetornaNull() {
        LoginDto login = new LoginDto();
        login.setUsuario("hacker");
        login.setClave("Velmore2024$");

        Usuario resultado = authService.autenticar(login);

        assertNull(resultado, "Debe retornar null para usuario no registrado");
    }

    @Test
    @DisplayName("Credenciales completamente incorrectas deben retornar null")
    void loginConCredencialesInvalidasRetornaNull() {
        LoginDto login = new LoginDto();
        login.setUsuario("usuario_malo");
        login.setClave("clave_mala");

        assertNull(authService.autenticar(login));
    }

    // ── Valores nulos y vacíos ────────────────────────────────────────────────

    @Test
    @DisplayName("LoginDto nulo debe retornar null sin lanzar excepción")
    void loginNuloRetornaNull() {
        // Prueba de robustez: el servicio no debe explotar con input nulo
        Usuario resultado = authService.autenticar(null);
        assertNull(resultado, "Un login nulo debe retornar null de forma segura");
    }

    @Test
    @DisplayName("Usuario y clave vacíos deben retornar null")
    void loginConCamposVaciosRetornaNull() {
        LoginDto login = new LoginDto();
        login.setUsuario("");
        login.setClave("");

        assertNull(authService.autenticar(login));
    }

    @Test
    @DisplayName("Clave vacía con usuario válido debe retornar null")
    void loginConClaveVaciaRetornaNull() {
        LoginDto login = new LoginDto();
        login.setUsuario("admin");
        login.setClave("");

        assertNull(authService.autenticar(login));
    }

    // ── Manejo de espacios (Apache Commons StringUtils) ───────────────────────

    @Test
    @DisplayName("Credenciales con espacios iniciales y finales deben autenticar correctamente")
    void loginConEspaciosExtraDebeAutenticar() {
        // Apache Commons StringUtils.trimToEmpty elimina espacios → login debe funcionar
        LoginDto login = new LoginDto();
        login.setUsuario("  admin  ");
        login.setClave("  Velmore2024$  ");

        Usuario resultado = authService.autenticar(login);

        assertNotNull(resultado, "StringUtils.trim debe permitir autenticar con espacios extra");
        assertTrue(resultado.esAdministrador());
    }

    @Test
    @DisplayName("El nombre completo del administrador debe ser no vacío")
    void adminTieneNombreCompleto() {
        LoginDto login = new LoginDto();
        login.setUsuario("admin");
        login.setClave("Velmore2024$");

        Usuario resultado = authService.autenticar(login);

        assertNotNull(resultado.getNombre());
        assertFalse(resultado.getNombre().isBlank(),
                "El administrador debe tener un nombre asignado");
    }
}
