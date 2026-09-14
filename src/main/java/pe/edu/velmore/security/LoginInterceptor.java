package pe.edu.velmore.security;

import pe.edu.velmore.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        Object sesion = request.getSession().getAttribute(SecurityConstants.USER_SESSION);

        if (!(sesion instanceof Usuario)) {
            response.sendRedirect("/login");
            return false;
        }

        Usuario usuario = (Usuario) sesion;
        if (!usuario.esAdministrador()) {
            response.sendRedirect("/perfil");
            return false;
        }

        return true;
    }
}
