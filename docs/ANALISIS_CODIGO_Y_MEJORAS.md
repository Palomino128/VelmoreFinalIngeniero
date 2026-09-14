# Análisis del código y mejoras aplicadas

## 1. Librerías solicitadas por la rúbrica

El proyecto incorpora todas las librerías de apoyo solicitadas:

| Librería | Archivo donde se usa | Propósito |
|---|---|---|
| Google Guava | `ProductoServiceImpl.java`, `InMemoryProductoDao.java`, `ProductoValidator.java` | Caché del catálogo, listas inmutables, Preconditions y mejora de eficiencia |
| Apache POI | `ReporteExcelService.java` | Generación de reporte Excel `.xlsx` |
| Apache Commons Lang | `ProductoValidator.java`, `AuthServiceImpl.java`, `ContactoServiceImpl.java` | Validación segura de textos, limpieza de entradas y comparación de cadenas |
| Logback | `logback-spring.xml` y servicios Java | Registro de eventos, errores e intentos de acceso |

## 2. Seguridad aplicada

Se mejoró la seguridad básica del tratamiento de información mediante:

- Login de administrador.
- Protección de rutas `/admin/**` y `/reporte/**` con interceptor.
- Control de sesión.
- Logout con invalidación de sesión.
- Cabeceras de seguridad: `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy` y `Cache-Control`.
- Validación de entradas antes de registrar productos.
- No se registran contraseñas en logs.
- Credenciales configurables mediante variables de entorno.

## 3. UI/UX y alcance funcional

La interfaz cubre el 100% del alcance comprometido:

- Inicio del sistema.
- Catálogo de perfumes.
- Búsqueda por nombre, marca, descripción o categoría.
- Detalle del producto.
- Contacto mediante mensaje generado para WhatsApp.
- Login administrativo.
- Panel administrativo.
- Registro, edición y eliminación de productos.
- Descarga de reporte Excel.

## 4. Coherencia y buenas prácticas

El código se encuentra organizado por paquetes:

- `controller`: controla las rutas.
- `service`: contiene la lógica de negocio.
- `dao`: separa el acceso a datos.
- `model`: representa las entidades.
- `dto`: transporta datos de formularios.
- `validation`: valida reglas de negocio.
- `security`: centraliza seguridad.

## 5. Git y GitHub

El proyecto incluye `.gitignore`, estructura preparada para control de versiones y guía para subir el 100% de avances a GitHub. En la entrega real se debe mostrar el repositorio con commits, README y archivos del proyecto.
