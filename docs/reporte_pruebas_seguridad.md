# Reporte de Pruebas de Seguridad — Proyecto VELMORE
**Taller de Pruebas de Seguridad | Avance: 70%**

---

## 1. Información General

| Campo | Detalle |
|-------|---------|
| **Proyecto** | VelmoreFinalIngeniero — Sistema de Gestión de Perfumería |
| **Versión analizada** | 1.0.0 |
| **Stack tecnológico** | Spring Boot 3.2.5, Java 17, Thymeleaf, JPA/MySQL, Google Guava |
| **Fecha del análisis** | Junio 2026 |
| **Metodología** | OWASP Top 10 2021 + Revisión Estática de Código (SAST manual) |
| **Entorno** | Desarrollo local (localhost:8080) |

---

## 2. Metodología Aplicada

Se realizaron las siguientes actividades:

1. **Revisión Estática de Código (SAST Manual)**: Análisis línea a línea de controladores, servicios y configuraciones.
2. **Mapeo OWASP Top 10 2021**: Cada hallazgo clasificado según categoría del estándar OWASP.
3. **Análisis de Flujo de Datos**: Seguimiento del flujo de información desde la entrada del usuario hasta las respuestas.
4. **Pruebas Documentadas en Código**: Vulnerabilidades respaldadas por pruebas en `SecurityTest.java` con evidencia ejecutable.

---

## 3. Resumen Ejecutivo

Se identificaron **8 observaciones de seguridad** en el proyecto VELMORE:

| Severidad | Cantidad | Porcentaje |
|-----------|----------|------------|
| 🔴 **Alta** | 4 | 50% |
| 🟡 **Media** | 3 | 37.5% |
| 🟢 **Baja** | 1 | 12.5% |
| **Total** | **8** | **100%** |

> **Conclusión**: El proyecto presenta vulnerabilidades críticas en autenticación y control de acceso que deben remediarse antes de cualquier despliegue en producción.

---

## 4. Hallazgos Detallados

---

### SEC-01 | Credenciales Hardcodeadas en Código Fuente
**Clasificación OWASP**: A07 — Identification and Authentication Failures  
**Severidad**: 🔴 **ALTA** | **Estado**: ⚠️ Pendiente de remediación

#### Descripción
Las credenciales `admin/123456` y `cliente/123456` están escritas directamente en `AuthServiceImpl.java`.

#### Evidencia
```java
// AuthServiceImpl.java — Línea 24
if (StringUtils.equals(usuario, "admin") && StringUtils.equals(clave, "123456")) {
    return new Usuario("admin", "", "Administrador VELMORE", "ADMIN");
}
```

#### Impacto
- Cualquier persona con acceso al repositorio obtiene las credenciales.
- El JAR compilado expone las credenciales mediante ingeniería inversa (JD-GUI, CFR).

#### Remediación
```java
// Usar base de datos de usuarios con PasswordEncoder de Spring Security
@Autowired PasswordEncoder passwordEncoder;
// Comparar: passwordEncoder.matches(rawPassword, storedHash)
```

---

### SEC-02 | Contraseña Comparada en Texto Plano
**Clasificación OWASP**: A02 — Cryptographic Failures  
**Severidad**: 🔴 **ALTA** | **Estado**: ⚠️ Pendiente de remediación

#### Descripción
Las contraseñas se comparan como texto plano sin hashing (BCrypt, Argon2, PBKDF2).

#### Evidencia
```java
// AuthServiceImpl.java — StringUtils.equals compara texto plano
if (StringUtils.equals(usuario, "admin") && StringUtils.equals(clave, "123456"))
```

#### Impacto
- "123456" aparece en cualquier diccionario de contraseñas comunes.
- Incumple NIST SP 800-63B y OWASP Authentication Cheat Sheet.

#### Remediación
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

---

### SEC-03 | Ausencia de Protección CSRF en Formularios POST
**Clasificación OWASP**: A01 — Broken Access Control  
**Severidad**: 🔴 **ALTA** | **Estado**: ⚠️ Pendiente de remediación

#### Descripción
Los endpoints POST del AdminController no tienen protección CSRF. Spring Security no está configurado.

#### Evidencia
```java
// AdminController.java — Sin token CSRF
@PostMapping("/admin/productos")
public String registrar(@ModelAttribute Producto producto, Model model) { ... }

@PostMapping("/admin/productos/{id}/eliminar")
public String eliminar(@PathVariable Long id) { ... }
```

#### Escenario de Ataque
1. Administrador autenticado visita una web maliciosa.
2. La web maliciosa envía un POST automático a `/admin/productos/{id}/eliminar`.
3. El navegador incluye la cookie de sesión → el servidor acepta → producto eliminado.

#### Remediación
```java
// SecurityConfig.java
http.csrf(csrf -> csrf.csrfTokenRepository(
    CookieCsrfTokenRepository.withHttpOnlyFalse()
));
```

---

### SEC-04 | Sin Verificación de Sesión en Endpoint `/admin`
**Clasificación OWASP**: A01 — Broken Access Control  
**Severidad**: 🔴 **ALTA** | **Estado**: ⚠️ Pendiente de remediación

#### Descripción
`AdminController.admin()` no verifica si el usuario tiene una sesión válida antes de mostrar el panel.

#### Evidencia
```java
// AdminController.java — Líneas 54-58
@GetMapping("/admin")
public String admin(Model model) {
    cargarDatos(model);   // ← Sin verificar sesión activa
    return "admin";
}
```

#### Impacto
- Acceso no autorizado al panel de administración.
- Exposición de inventario y precios del negocio.

#### Remediación
```java
@GetMapping("/admin")
public String admin(Model model, HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(SecurityConstants.USER_SESSION);
    if (usuario == null || !usuario.esAdministrador()) {
        return "redirect:/login";
    }
    cargarDatos(model);
    return "admin";
}
```

---

### SEC-05 | Falta de Validación de Rango en IDs de Recursos
**Clasificación OWASP**: A01 — Broken Access Control  
**Severidad**: 🟡 **MEDIA** | **Estado**: ⚠️ Pendiente de remediación

#### Descripción
El endpoint de eliminación acepta cualquier `Long` como ID sin validar que sea positivo.

#### Evidencia
```java
// AdminController.java — Línea 72-75
@PostMapping("/admin/productos/{id}/eliminar")
public String eliminar(@PathVariable Long id) {  // ← Sin @Min(1)
    productoService.eliminar(id);
    return "redirect:/admin";
}
```

#### Remediación
```java
// Opción 1: Jakarta Bean Validation
@PathVariable @Min(value = 1, message = "ID debe ser positivo") Long id

// Opción 2: Validación en el servicio
if (id == null || id <= 0) {
    throw new IllegalArgumentException("ID inválido: debe ser mayor que cero");
}
```

---

### SEC-06 | XSS Potencial en Mensajes de Carrito
**Clasificación OWASP**: A03 — Injection  
**Severidad**: 🟡 **MEDIA** | **Estado**: ⚠️ Pendiente de verificación en vistas

#### Descripción
`CarritoService.generarMensaje()` concatena el nombre del cliente sin sanitización HTML. Si una vista usa `th:utext` (no escapado), se habilita un vector XSS.

#### Evidencia
```java
// CarritoService.java — Línea 47
String cliente = nombreCliente.trim();
mensaje.append("Hola, soy ").append(cliente); // ← Sin sanitización HTML
```

#### Escenario de Ataque (si la vista usa th:utext)
```
nombreCliente = "<script>document.cookie='stolen='+document.cookie</script>"
```

#### Remediación
```html
<!-- CORRECTO: Thymeleaf escapa automáticamente con th:text -->
<p th:text="${mensaje}">...</p>

<!-- INCORRECTO: th:utext renderiza HTML sin escape (vulnerable) -->
<p th:utext="${mensaje}">...</p>
```

---

### SEC-07 | Sin Límite de Intentos de Login (Brute Force)
**Clasificación OWASP**: A07 — Identification and Authentication Failures  
**Severidad**: 🟡 **MEDIA** | **Estado**: ⚠️ Pendiente de remediación

#### Descripción
El sistema no implementa rate limiting, bloqueo de cuenta ni CAPTCHA. Intentos ilimitados sin consecuencias.

#### Evidencia
```java
// AdminController.java — Línea 31-46
@PostMapping("/login")
public String procesarLogin(@ModelAttribute LoginDto login, ...) {
    // ← Sin registro de intentos fallidos
    // ← Sin delay entre intentos
    // ← Sin bloqueo de cuenta
    if (usuario == null) {
        return "login"; // ← Reintentar inmediatamente
    }
}
```

#### Remediación usando Google Guava (ya es dependencia del proyecto)
```java
// Usando Guava RateLimiter (ya disponible en el proyecto)
import com.google.common.util.concurrent.RateLimiter;

private final RateLimiter loginRateLimiter = RateLimiter.create(0.5); // 1 intento cada 2 seg

@PostMapping("/login")
public String procesarLogin(...) {
    loginRateLimiter.acquire(); // Aplica delay si se excede la tasa
    // ... resto del login
}
```

---

### SEC-08 | Número de WhatsApp Expuesto en Respuestas HTTP
**Clasificación OWASP**: A09 — Security Logging and Monitoring Failures  
**Severidad**: 🟢 **BAJA** | **Estado**: ✅ Aceptado (riesgo bajo para el negocio)

#### Descripción
El número de WhatsApp queda en texto claro en las URLs generadas, facilitando scraping automatizado.

#### Evidencia
```java
// CarritoService.java — Línea 84
return "https://wa.me/" + whatsAppProperties.getNumero() + "?text=" + textoCodificado;
// → https://wa.me/51999999999?text=...
```

#### Evaluación de Riesgo
- El número es información de contacto pública del negocio.
- La exposición es intencional (botón de compra por WhatsApp).
- Riesgo principal: spam automatizado al número.

#### Remediación (opcional)
- Implementar endpoint proxy que genere el link en el backend.
- Agregar rate limiting al endpoint de generación de links.

---

## 5. Tabla Resumen de Hallazgos

| ID | Vulnerabilidad | Archivo:Línea | OWASP 2021 | Severidad | Estado |
|----|---------------|---------------|-----------|-----------|--------|
| SEC-01 | Credenciales hardcodeadas | `AuthServiceImpl.java:24` | A07 | 🔴 Alta | Pendiente |
| SEC-02 | Contraseña en texto plano | `AuthServiceImpl.java:24` | A02 | 🔴 Alta | Pendiente |
| SEC-03 | Sin protección CSRF | `AdminController.java:60-76` | A01 | 🔴 Alta | Pendiente |
| SEC-04 | Sin validación de sesión | `AdminController.java:54-58` | A01 | 🔴 Alta | Pendiente |
| SEC-05 | Sin validación de rango de ID | `AdminController.java:72` | A01 | 🟡 Media | Pendiente |
| SEC-06 | XSS potencial en mensajes | `CarritoService.java:47` | A03 | 🟡 Media | Verificar |
| SEC-07 | Sin rate limiting en login | `AdminController.java:31` | A07 | 🟡 Media | Pendiente |
| SEC-08 | Número WhatsApp expuesto | `CarritoService.java:84` | A09 | 🟢 Baja | Aceptado |

---

## 6. Herramientas y Referencias

### Herramientas de análisis utilizadas
- **Revisión Manual SAST**: Análisis estático del código fuente
- **JUnit 5 + SecurityTest.java**: Pruebas automatizadas de seguridad (8 hallazgos documentados)

### Referencias OWASP
- [OWASP Top 10 2021](https://owasp.org/Top10/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP CSRF Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)
- [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- [OWASP XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)

### Librerías recomendadas para remediación

| Librería | Uso | Disponibilidad |
|----------|-----|---------------|
| `spring-boot-starter-security` | Autenticación + CSRF + roles | Agregar a pom.xml |
| `BCryptPasswordEncoder` | Hashing de contraseñas | Incluido en Spring Security |
| `Guava RateLimiter` | Rate limiting en login | **Ya disponible** (Guava 33.2.1) |
| `OWASP AntiSamy` | Sanitización HTML | Agregar a pom.xml |

---

## 7. Recomendaciones Prioritarias

### Prioridad ALTA (antes de producción)
1. Integrar **Spring Security** con autenticación BD, BCrypt y control de acceso por roles.
2. Eliminar **credenciales hardcodeadas** y migrar usuarios a tabla MySQL.
3. Habilitar **protección CSRF** en todos los formularios POST.
4. Agregar **verificación de sesión** en todos los endpoints protegidos.

### Prioridad MEDIA (siguiente sprint)
5. Agregar **validación `@Min(1)`** a los parámetros de tipo ID.
6. Implementar **rate limiting** en el endpoint de login con `Guava RateLimiter`.
7. Auditar todas las vistas Thymeleaf para garantizar uso de **`th:text`** (no `th:utext`).

### Prioridad BAJA (evaluar según necesidad)
8. Revisar exposición del número WhatsApp según política de privacidad del negocio.

---

*Reporte elaborado para el Taller de Pruebas de Seguridad — Proyecto VELMORE*  
*Metodología: OWASP Top 10 2021 | Análisis estático + pruebas automatizadas*
