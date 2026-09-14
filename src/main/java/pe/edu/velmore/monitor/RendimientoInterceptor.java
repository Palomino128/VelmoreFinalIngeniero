package pe.edu.velmore.monitor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * CAPA 4 — Interceptor de Rendimiento HTTP.
 *
 * <p>Mide la latencia de cada petición HTTP y la registra automáticamente
 * en el log de rendimiento ({@code logs/velmore-rendimiento.log}).</p>
 *
 * <h3>Formato del log:</h3>
 * <pre>
 * [PERF]      GET  /catalogo                → 23ms   | status=200
 * [PERF]      POST /contacto/mensaje        → 45ms   | status=200
 * [PERF SLOW] GET  /reporte/excel           → 1234ms | status=200  ← alerta
 * </pre>
 *
 * <h3>Umbrales:</h3>
 * <ul>
 *   <li>&lt; 500ms  → normal (INFO)</li>
 *   <li>500–999ms → lento  (WARN con etiqueta [PERF SLOW])</li>
 *   <li>≥ 1000ms  → crítico (WARN con etiqueta [PERF CRITICAL])</li>
 * </ul>
 *
 * <p>El interceptor se registra en {@link pe.edu.velmore.config.WebConfig}
 * para todas las rutas excepto recursos estáticos ({@code /css/**}, etc.).</p>
 */
@Component
public class RendimientoInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RendimientoInterceptor.class);

    // Umbrales de latencia (en milisegundos)
    private static final long UMBRAL_LENTO    = 500L;
    private static final long UMBRAL_CRITICO  = 1000L;

    // Atributo de request para almacenar el tiempo de inicio
    private static final String ATTR_INICIO = "velmore_request_start";

    /**
     * Se ejecuta ANTES del controlador: registra el timestamp de inicio.
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        request.setAttribute(ATTR_INICIO, System.currentTimeMillis());
        return true; // continuar procesamiento
    }

    /**
     * Se ejecuta DESPUÉS del controlador: calcula latencia y registra el log.
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {

        Long inicio = (Long) request.getAttribute(ATTR_INICIO);
        if (inicio == null) return;

        long latencia = System.currentTimeMillis() - inicio;
        String metodo  = request.getMethod();
        String uri     = request.getRequestURI();
        int    status  = response.getStatus();

        // Ignorar recursos estáticos (no aportan información de rendimiento)
        if (esRecursoEstatico(uri)) return;

        // Formato del mensaje de rendimiento
        String resumen = String.format("%-6s %-35s → %4dms | status=%d",
                metodo, uri, latencia, status);

        if (latencia >= UMBRAL_CRITICO) {
            // Petición crítica — puede indicar un problema en BD o caché
            log.warn("[PERF CRITICAL] {} ← revisar inmediatamente", resumen);
        } else if (latencia >= UMBRAL_LENTO) {
            // Petición lenta — vigilar si se repite
            log.warn("[PERF SLOW]     {}", resumen);
        } else {
            // Petición normal
            log.info("[PERF]          {}", resumen);
        }

        // Log de error si la petición terminó con fallo
        if (ex != null) {
            log.error("[PERF ERROR]    {} | exception={}", resumen, ex.getMessage());
        }
    }

    /**
     * Filtra recursos estáticos que no necesitan monitoreo de latencia.
     */
    private boolean esRecursoEstatico(String uri) {
        return uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/img/")
                || uri.startsWith("/images/")
                || uri.startsWith("/fonts/")
                || uri.startsWith("/favicon")
                || uri.endsWith(".ico")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".svg");
    }
}
