package pe.edu.velmore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.edu.velmore.monitor.RendimientoInterceptor;
import pe.edu.velmore.security.LoginInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CAPA 4 — Interceptor de rendimiento inyectado por Spring
    private final RendimientoInterceptor rendimientoInterceptor;

    public WebConfig(RendimientoInterceptor rendimientoInterceptor) {
        this.rendimientoInterceptor = rendimientoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor de seguridad: protege rutas de administración y reportes
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/admin/**", "/reporte/**");

        // Interceptor de rendimiento: mide latencia de TODAS las rutas de negocio
        // Excluye /actuator (monitoreo propio de Spring) y /error
        registry.addInterceptor(rendimientoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/error", "/css/**",
                        "/js/**", "/img/**", "/images/**", "/fonts/**");
    }
}

