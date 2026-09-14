# 📦 CHANGELOG — VelmoreFinalIngeniero

## [v1.0] — 2026-09-13

### ✨ Nuevas funcionalidades
- `feat`: Endpoint `/catalogo/destacados` — muestra top 6 productos activos
- `feat`: Logging SLF4J en `CatalogoController` para trazabilidad
- `feat`: `ProductoAnalisis` — utilidad de análisis de catálogo
  - `calcularPrecioPromedio()` — precio promedio del catálogo
  - `contarStockBajo()` — alerta de stock crítico
  - `filtrarEconomicos()` — productos bajo S/30.00
  - `aplicarDescuento()` — cálculo de precio con descuento
  - `productoMasCaro()` / `productoMenosStock()` — extremos del catálogo
  - `generarResumen()` — estadísticas completas
- `feat`: Control de intentos de login — `LoginIntentoService`
  - Bloqueo automático tras 3 intentos fallidos
  - Desbloqueo automático tras 10 minutos
- `feat`: Nuevas constantes en `SecurityConstants`
  - `MAX_INTENTOS_LOGIN = 3`
  - `MINUTOS_BLOQUEO = 10`
  - `DURACION_SESION_MIN = 30`
  - `PRECIO_MAXIMO_CATALOGO = 999.99`
  - `MAX_LONGITUD_NOMBRE = 150`

### 📚 Documentación
- `docs`: `GIT_INTRODUCCION.md` — conceptos de control de versiones
- `docs`: `GIT_COMANDOS.md` — referencia completa de comandos Git

### 🔧 Resolución de conflictos
- `fix`: Conflicto resuelto entre `feature/autenticacion-mejorada` y
  `feature/mejora-catalogo` en `SecurityConstants.MAX_INTENTOS_LOGIN`
  → Decisión: valor 3 (política más segura)

---

## [v0.1] — 2026-09-13 (commit inicial)
- Estructura base del proyecto Spring Boot
- Controllers, Services, Models, DAOs, DTOs
- Templates Thymeleaf + recursos estáticos
- Tests unitarios (6 clases)
- Scripts de despliegue (`deploy/`)
