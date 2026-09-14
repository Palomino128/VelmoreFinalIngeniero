# Mapa de rúbrica y código

| Criterio | Evidencia en código |
|---|---|
| Librerías de apoyo | `pom.xml`, `ProductoServiceImpl`, `ReporteExcelService`, `ProductoValidator`, `logback-spring.xml` |
| Google Guava | Caché en `ProductoServiceImpl`, listas en `InMemoryProductoDao` |
| Apache POI | Exportación Excel en `ReporteExcelService` |
| Apache Commons | Validaciones en `ProductoValidator`, `AuthServiceImpl`, `ContactoServiceImpl` |
| Logback | Logs en servicios y `logback-spring.xml` |
| Seguridad | `LoginInterceptor`, `SecurityHeadersInterceptor`, `WebConfig`, `AuthServiceImpl` |
| Git/GitHub | `.gitignore`, `docs/EVIDENCIA_GITHUB.md`, historial de commits al subir a GitHub |
| UI/UX | `templates/*.html`, `static/styles.css`, `static/app.js` |
| Alcance completo | Catálogo, búsqueda, detalle, contacto, login, admin, CRUD, reporte Excel |
| Buenas prácticas | MVC, DAO, servicios, validadores, DTO, separación por paquetes |
