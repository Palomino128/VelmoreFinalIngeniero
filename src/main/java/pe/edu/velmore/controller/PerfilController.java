package pe.edu.velmore.controller;

import pe.edu.velmore.model.Usuario;
import pe.edu.velmore.security.SecurityConstants;
import pe.edu.velmore.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerfilController {
    private final ProductoService productoService;

    public PerfilController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        Object sesion = session.getAttribute(SecurityConstants.USER_SESSION);

        if (!(sesion instanceof Usuario)) {
            return "redirect:/login";
        }

        Usuario usuario = (Usuario) sesion;
        model.addAttribute("usuario", usuario);
        model.addAttribute("productos", productoService.listarActivos().stream().limit(4).toList());

        if (usuario.esAdministrador()) {
            return "redirect:/admin";
        }

        return "perfil";
    }
}
