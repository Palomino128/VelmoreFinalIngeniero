# 📸 Evidencia de Control de Versiones en GitHub

El proyecto **VelmoreFinalIngeniero** implementa un flujo completo de control de versiones siguiendo las mejores prácticas y la estrategia **GitFlow**.

## 🌐 Repositorio Remoto
**URL:** [https://github.com/Palomino128/VelmoreFinalIngeniero](https://github.com/Palomino128/VelmoreFinalIngeniero)

## 🌿 Estructura de Ramas Creadas

Se han creado y publicado las siguientes ramas, evidenciando el dominio de GitFlow:

1. `main`: Rama principal de producción.
2. `develop`: Rama de integración principal.
3. `feature/autenticacion-mejorada`: Desarrollo de nueva funcionalidad (límites de login).
4. `feature/mejora-catalogo`: Desarrollo paralelo para forzar y resolver un conflicto.
5. `release/v1.0`: Rama de preparación para el pase a producción, incluye `CHANGELOG.md` y un Tag `v1.0`.
6. `hotfix/stock-critico`: Parche urgente derivado de `main` para arreglar un problema en producción sin afectar el desarrollo actual.

## 🔀 Resolución de Conflictos (Merge Conflict)

Se forzó intencionalmente un conflicto modificando la constante `MAX_INTENTOS_LOGIN` en `SecurityConstants.java` desde dos ramas distintas:
- `feature/autenticacion-mejorada` la configuró en 3.
- `feature/mejora-catalogo` la configuró en 5.

**Resolución:** Durante el merge hacia `develop`, Git detectó el conflicto. Se resolvió manualmente editando el archivo, conservando la política más estricta (`3`) y fusionando el resto de constantes, demostrando capacidad de resolución manual de código.

## 🤝 Colaboración y Pull Requests

Se generaron múltiples **Pull Requests (PRs)** en GitHub para evidenciar el flujo colaborativo y de revisión de código antes de integrar a `main`:

- **PR #1:** `feature/mi-funcion` → `main` (Mergeado exitosamente)
- **PR #2:** `release/v1.0` → `main` (Preparación de Release v1.0)
- **PR #3:** `hotfix/stock-critico` → `main` (Integración de parche de emergencia)

Todos los PRs incluyen una descripción detallada (`body`) con el propósito, cambios y checklist de revisión.

## 📚 Documentación Técnica

El repositorio incluye la documentación teórica y práctica exigida:
- `docs/GIT_INTRODUCCION.md`: Conceptos y principios básicos de SCV.
- `docs/GIT_COMANDOS.md`: Guía de instalación, configuración, comandos y flujo usado.

---
*Documento generado para evidenciar el cumplimiento de la rúbrica de Control de Versiones.*
