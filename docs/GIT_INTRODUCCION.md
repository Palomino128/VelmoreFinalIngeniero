# 📘 Introducción al Control de Versiones — Git

## ¿Qué es el control de versiones?

Un **Sistema de Control de Versiones (SCV)** registra los cambios realizados sobre
archivos a lo largo del tiempo, permitiendo recuperar versiones anteriores, comparar
cambios y colaborar en equipo sin sobreescribir el trabajo de otros.

## Tipos de SCV

| Tipo | Descripción | Ejemplo |
|------|-------------|---------|
| **Local** | Historial solo en tu máquina | RCS |
| **Centralizado** | Un servidor central, todos conectados a él | SVN, CVS |
| **Distribuido** | Cada desarrollador tiene una copia completa | **Git**, Mercurial |

## ¿Por qué Git?

- **Distribuido**: cada clon ES el repositorio completo con todo el historial.
- **Rápido**: operaciones locales (commit, log, diff) sin red.
- **Branching barato**: crear ramas es instantáneo y no copia archivos.
- **Integridad**: cada commit tiene un hash SHA-1 único e irrepetible.
- **Estándar de la industria**: usado por Google, Microsoft, Meta, etc.

## Conceptos fundamentales

| Concepto | Definición |
|----------|------------|
| **Repositorio** | Carpeta `.git/` con todo el historial del proyecto |
| **Commit** | Foto instantánea del proyecto en un momento dado |
| **Rama (branch)** | Línea paralela de desarrollo independiente |
| **HEAD** | Puntero al commit actual donde estás trabajando |
| **Staging Area** | Zona intermedia antes de hacer commit (`git add`) |
| **Remote** | Copia del repositorio en un servidor (GitHub) |
| **Merge** | Integrar los cambios de una rama en otra |
| **Conflicto** | Dos ramas modificaron la misma línea → requiere resolución manual |

## Flujo básico de trabajo

```
Directorio de trabajo  →  git add  →  Staging Area  →  git commit  →  Repositorio local
                                                                              ↓
                                                                        git push
                                                                              ↓
                                                                    Repositorio remoto (GitHub)
```

## Este proyecto usa GitFlow

```
main       ──●────────────────────────────────●── (producción)
              \                              /
develop        ●──────────────────────●─────    (integración)
                \              \     /
feature/*        ●──●           ●───            (nuevas funciones)
                                      \
release/*                              ●──●     (preparar entrega)
                                             \
hotfix/*       ──────────────────────────────●  (parche urgente)
```

---
*Proyecto: VelmoreFinalIngeniero | Autor: Edu Palomino*
