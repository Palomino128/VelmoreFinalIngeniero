# ⚙️ Instalación, Configuración y Comandos Git

## 1. Instalación

### Windows
Descargar desde: https://git-scm.com/download/win
Verificar: `git --version`  → `git version 2.55.0.windows.5`

---

## 2. Configuración inicial (una sola vez)

```bash
# Identidad del desarrollador
git config --global user.name  "Edu Palomino"
git config --global user.email "edupalomino728@gmail.com"

# Editor predeterminado
git config --global core.editor "code --wait"

# Fin de línea (Windows)
git config --global core.autocrlf true

# Ver toda la configuración
git config --list
```

---

## 3. Comandos básicos

### Iniciar / Clonar

| Comando | Qué hace |
|---------|----------|
| `git init` | Inicializa repositorio local vacío |
| `git clone <url>` | Clona repositorio remoto completo |

```bash
# Cómo se usó en este proyecto:
git init
git clone https://github.com/Palomino128/VelmoreFinalIngeniero.git
```

### Estado y Staging

| Comando | Qué hace |
|---------|----------|
| `git status` | Muestra archivos modificados / sin trackear |
| `git add <archivo>` | Agrega archivo al staging area |
| `git add .` | Agrega TODOS los cambios al staging |
| `git diff` | Diferencias no staged |
| `git diff --staged` | Diferencias staged vs último commit |

### Commits

| Comando | Qué hace |
|---------|----------|
| `git commit -m "mensaje"` | Crea commit con mensaje |
| `git log --oneline` | Historial compacto |
| `git log --graph` | Historial con árbol de ramas |
| `git show <hash>` | Ver detalle de un commit |

```bash
# Convención de mensajes en este proyecto:
git commit -m "feat: nueva funcionalidad"
git commit -m "fix: corrección de bug"
git commit -m "docs: actualizar documentación"
git commit -m "chore: tarea de mantenimiento"
git commit -m "hotfix: parche urgente en producción"
```

### Ramas

| Comando | Qué hace |
|---------|----------|
| `git branch` | Lista ramas locales |
| `git branch -a` | Lista ramas locales y remotas |
| `git checkout -b <rama>` | Crea y cambia a nueva rama |
| `git checkout <rama>` | Cambia de rama |
| `git merge <rama>` | Fusiona rama en la actual |
| `git branch -d <rama>` | Elimina rama local |

### Repositorio remoto

| Comando | Qué hace |
|---------|----------|
| `git remote add origin <url>` | Conecta con repositorio remoto |
| `git remote -v` | Ver remotes configurados |
| `git push origin <rama>` | Sube rama al remoto |
| `git push -u origin main` | Sube y establece tracking |
| `git pull origin main` | Descarga + fusiona cambios del remoto |
| `git fetch` | Descarga sin fusionar |

### Resolución de conflictos

```bash
# Cuando git merge genera conflicto:
git status                    # ver archivos en conflicto
# editar archivos → eliminar marcadores <<<, ===, >>>
git add <archivo-resuelto>    # marcar como resuelto
git commit -m "fix: resolver conflicto entre ramas"
git push origin <rama>
```

---

## 4. Historial de comandos usados en VelmoreFinalIngeniero

```bash
git config --global user.name "Edu Palomino"
git config --global user.email "edupalomino728@gmail.com"
git init
git add .
git commit -m "chore: commit inicial - proyecto VelmoreFinalIngeniero"
git branch -M main
git remote add origin https://github.com/Palomino128/VelmoreFinalIngeniero.git
git push -u origin main
git checkout -b feature/mi-funcion
git push origin feature/mi-funcion
git checkout -b develop
git checkout -b feature/autenticacion-mejorada
git checkout -b feature/mejora-catalogo
git merge feature/autenticacion-mejorada
git checkout -b release/v1.0
git tag -a v1.0 -m "Release versión 1.0"
git checkout -b hotfix/stock-critico
```

---
*Proyecto: VelmoreFinalIngeniero | Autor: Edu Palomino*
