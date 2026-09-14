# Memoria Descriptiva y Sustento de Proyecto — VELMORE
**Documento para Defensa y Sustentación (Estándar 3 pts)**

Este documento demuestra cómo el proyecto **VELMORE** cumple integralmente con los 4 criterios de evaluación finales para la construcción de una solución informática profesional.

---

## 1. Completitud (Solución Completa)
*El proyecto cubre el 100% del alcance comprometido en los talleres.*

*   **Taller de Testing (60%)**: 64+ pruebas unitarias implementadas usando **JUnit 5 con stubs manuales** (sin dependencia de Mockito), alcanzando excelente cobertura. Las pruebas validan servicios de negocio, DAOs simulados y manejo de errores.
*   **Taller de Pruebas de Seguridad (70%)**: Implementación de interceptores de autenticación, saneamiento de entradas y manejo de sesiones. Todo respaldado por un **Reporte de Pruebas de Seguridad** formal.
*   **Taller de Despliegue (80%)**: Separación en perfiles (Dev/Prod), externalización de variables sensibles (`.env.example`), generación de un **Fat JAR** autocontenido y scripts de despliegue automatizados (`.bat`).
*   **Taller de Monitoreo (90%)**: Integración de **Logback** con archivos rotativos, métricas de negocio con **Micrometer**, y Health Indicators personalizados vía **Spring Actuator**.
*   **Taller de Mantenimiento (100%)**: Creación de **Cron Jobs** (`@Scheduled`) para refresco automático de cachés, scripts de limpieza temporales y exportación/backup de base de datos automatizado (`mysqldump`).

---

## 2. Coherencia (Documentación y Código Alineados)
*Existe una trazabilidad directa entre la documentación generada y el código fuente.*

*   **Arquitectura Reflejada en Docs**: El uso de cachés en RAM (`ProductoServiceImpl`) está formalmente documentado en el `PLAN_MANTENIMIENTO.md`, donde se justifica el uso del Cron Job de limpieza de madrugada a las 03:00 AM para prevenir Memory Leaks.
*   **Políticas de Monitoreo Aplicadas**: Los umbrales definidos en el `PLAN_MONITOREO.md` (ej. alertas de latencia a los >1000ms) se encuentran codificados exactamente igual en las constantes de `RendimientoInterceptor.java`.
*   **Despliegue Coherente**: La `GUIA_DESPLIEGUE.md` refleja la configuración exacta de los puertos (`8081` para `dev` y `8080` para `prod`) y las conexiones optimizadas de **HikariCP** aplicadas en los archivos `application-*.properties`.

---

## 3. Buenas Prácticas y Patrones de Diseño
*El proyecto emplea estándares robustos de la industria del software.*

*   **Inyección de Dependencias (IoC)**: Utilizado nativamente con Spring (`@Service`, `@Controller`). Facilita el aislamiento durante las pruebas unitarias y reduce el acoplamiento.
*   **Arquitectura en Capas**: Separación estricta entre presentación (`controller`), negocio (`service`), acceso a datos (`dao`) y transferencia de datos (`dto`).
*   **Patrón Singleton (Caché)**: Uso del `LoadingCache` de **Google Guava** como Singleton en la capa de servicios para acelerar la respuesta del catálogo sin saturar MySQL.
*   **Gestión de Dependencias (Maven)**: Uso de `pom.xml` para empaquetado, manejo de perfiles (`-Pdev`, `-Pprod`), control de versión de Java (Compiler Plugin 17) e integración de librerías (`Apache POI`, `Guava`, `Actuator`).

---

## 4. Autoría y Dominio del Código
*Elementos clave que el estudiante debe conocer para sustentar su dominio del proyecto:*

*   **Flujo del Caché (Google Guava)**: Si el jurado pregunta *"¿Cómo optimizaste la base de datos?"*, la respuesta es: *"Se implementó un `LoadingCache` en `ProductoServiceImpl`. Cuando se busca un producto, primero consulta la RAM. Si no está (Cache Miss), va a MySQL. Cada escritura a la BD limpia este caché (Invalidate) para mantener la consistencia".*
*   **Monitoreo (Interceptor HTTP)**: Si preguntan *"¿Cómo sabes si el sistema está lento?"*, responder: *"Implementé el `RendimientoInterceptor`, que captura el timestamp al inicio de un Request y lo compara al final. Si la diferencia es mayor a 500ms, genera un log nivel `WARN` indicando degradación".*
*   **Testing con Stubs Manuales**: Para justificar las pruebas unitarias: *"Usamos `InMemoryProductoDao`, una implementación alternativa de la interfaz `ProductoDao` que almacena productos en memoria. Gracias a la inyección por constructor, podemos instanciar `ProductoServiceImpl` directamente con esta implementación sin necesitar Spring Context ni base de datos. Para `WhatsAppProperties` usamos un stub anónimo manual, compatible con Java 17–26 sin dependencia de ByteBuddy."*

---
*Este documento avala que la solución fue diseñada considerando altos estándares académicos y de ingeniería de software.*
