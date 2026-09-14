package pe.edu.velmore.controller;

import pe.edu.velmore.dto.LoginDto;
import pe.edu.velmore.model.Categoria;
import pe.edu.velmore.model.Producto;
import pe.edu.velmore.model.Usuario;
import pe.edu.velmore.security.SecurityConstants;
import pe.edu.velmore.service.AuthService;
import pe.edu.velmore.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {
    private final ProductoService productoService;
    private final AuthService authService;

    public AdminController(ProductoService productoService, AuthService authService) {
        this.productoService = productoService;
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute LoginDto login, Model model, HttpSession session) {
        Usuario usuario = authService.autenticar(login);

        if (usuario == null) {
            model.addAttribute("error", "Credenciales incorrectas");
            return "login";
        }

        session.setAttribute(SecurityConstants.USER_SESSION, usuario);

        if (usuario.esAdministrador()) {
            return "redirect:/admin";
        }

        return "redirect:/perfil";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        cargarDatos(model);
        return "admin";
    }

    @PostMapping("/admin/productos")
    public String registrar(@ModelAttribute Producto producto, Model model) {
        try {
            productoService.registrar(producto);
            return "redirect:/admin";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            cargarDatos(model);
            return "admin";
        }
    }

    @PostMapping("/admin/productos/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return "redirect:/admin";
    }

    private void cargarDatos(Model model) {
        model.addAttribute("productos", productoService.listarActivos());
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("totalProductos", productoService.totalProductos());
        model.addAttribute("totalCategorias", productoService.totalCategorias());
        model.addAttribute("valorInventario", productoService.valorInventario());
    }
}
