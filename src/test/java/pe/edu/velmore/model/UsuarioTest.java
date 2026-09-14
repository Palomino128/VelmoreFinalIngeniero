package pe.edu.velmore.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el modelo Usuario.
 *
 * Conceptos de Testing demostrados:
 * - Pruebas de modelo/POJO: verifican que el estado interno del objeto
 *   se construye y manipula correctamente.
 * - Pruebas case-insensitive: garantizan robustez ante variaciones de mayúsculas
 *   en el campo rol.
 * - Pruebas de invariante: esAdministrador() y esCliente() son mutuamente excluyentes.
 */
@DisplayName("Pruebas del modelo Usuario")
class UsuarioTest {

    // ── Constructor completo ──────────────────────────────────────────────────

    @Test
    @DisplayName("Constructor debe inicializar todos los campos correctamente")
    void constructorInicializaCamposCorrectamente() {
        Usuario usuario = new Usuario("admin", "secreto", "Administrador VELMORE", "ADMIN");

        assertEquals("admin", usuario.getUsuario());
        assertEquals("secreto", usuario.getClave());
        assertEquals("Administrador VELMORE", usuario.getNombre());
        assertEquals("ADMIN", usuario.getRol());
    }

    @Test
    @DisplayName("Constructor por defecto debe crear un usuario con campos nulos")
    void constructorPorDefectoCreaUsuarioConNulos() {
        Usuario usuario = new Usuario();

        assertNull(usuario.getUsuario());
        assertNull(usuario.getClave());
        assertNull(usuario.getNombre());
        assertNull(usuario.getRol());
    }

    // ── esAdministrador() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("esAdministrador() debe retornar true cuando el rol es 'ADMIN'")
    void esAdministradorConRolAdmin() {
        Usuario admin = new Usuario("admin", "", "Admin", "ADMIN");
        assertTrue(admin.esAdministrador());
    }

    @Test
    @DisplayName("esAdministrador() es case-insensitive: 'admin' también es válido")
    void esAdministradorEsCaseInsensitive() {
        Usuario admin = new Usuario("u", "", "N", "admin");
        assertTrue(admin.esAdministrador(),
                "equalsIgnoreCase debe reconocer 'admin' como ADMIN");
    }

    @Test
    @DisplayName("esAdministrador() debe retornar false cuando el rol es 'CLIENTE'")
    void esAdministradorFalsoParaCliente() {
        Usuario cliente = new Usuario("cliente", "", "Cliente", "CLIENTE");
        assertFalse(cliente.esAdministrador());
    }

    @Test
    @DisplayName("esAdministrador() debe retornar false cuando el rol es null")
    void esAdministradorFalsoParaRolNulo() {
        Usuario usuario = new Usuario();
        // equalsIgnoreCase en un null receptor lanzaría NPE, pero aquí "ADMIN".equalsIgnoreCase(null)
        // es false de forma segura porque la constante está en el lado izquierdo.
        assertFalse(usuario.esAdministrador(),
                "Un usuario sin rol no debe ser administrador");
    }

    // ── esCliente() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("esCliente() debe retornar true cuando el rol es 'CLIENTE'")
    void esClienteConRolCliente() {
        Usuario cliente = new Usuario("cliente", "", "Cliente VELMORE", "CLIENTE");
        assertTrue(cliente.esCliente());
    }

    @Test
    @DisplayName("esCliente() es case-insensitive: 'cliente' en minúsculas también es válido")
    void esClienteEsCaseInsensitive() {
        Usuario cliente = new Usuario("u", "", "N", "cliente");
        assertTrue(cliente.esCliente());
    }

    @Test
    @DisplayName("esCliente() debe retornar false cuando el rol es 'ADMIN'")
    void esClienteFalsoParaAdmin() {
        Usuario admin = new Usuario("admin", "", "Admin", "ADMIN");
        assertFalse(admin.esCliente());
    }

    // ── Invariante de exclusividad ────────────────────────────────────────────

    @Test
    @DisplayName("Un usuario ADMIN no puede ser CLIENTE al mismo tiempo")
    void adminNoEsClienteAlMismoTiempo() {
        Usuario admin = new Usuario("admin", "", "Admin", "ADMIN");
        assertTrue(admin.esAdministrador());
        assertFalse(admin.esCliente(), "ADMIN y CLIENTE son roles mutuamente excluyentes");
    }

    @Test
    @DisplayName("Un usuario CLIENTE no puede ser ADMIN al mismo tiempo")
    void clienteNoEsAdminAlMismoTiempo() {
        Usuario cliente = new Usuario("cliente", "", "Cliente", "CLIENTE");
        assertTrue(cliente.esCliente());
        assertFalse(cliente.esAdministrador(), "CLIENTE y ADMIN son roles mutuamente excluyentes");
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Los setters deben actualizar correctamente el estado del usuario")
    void settersActualizanEstadoCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setUsuario("nuevo_usuario");
        usuario.setClave("nueva_clave");
        usuario.setNombre("Nuevo Nombre");
        usuario.setRol("CLIENTE");

        assertEquals("nuevo_usuario", usuario.getUsuario());
        assertEquals("nueva_clave", usuario.getClave());
        assertEquals("Nuevo Nombre", usuario.getNombre());
        assertTrue(usuario.esCliente());
    }
}
