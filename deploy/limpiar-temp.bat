@echo off
setlocal
chcp 65001 >nul
:: ============================================================================
:: SCRIPT DE LIMPIEZA DE MANTENIMIENTO VELMORE
:: Taller de Mantenimiento
:: ============================================================================

echo ==============================================
echo [VELMORE] Iniciando limpieza de mantenimiento
echo ==============================================

set LOG_DIR=..\logs
set TARGET_DIR=..\target

echo [INFO] Limpiando logs antiguos (retencion gestionada por Logback, pero forzamos limpieza)...
if exist "%LOG_DIR%\*.log" (
    :: Elimina logs de rendimiento con más de 15 dias para liberar disco extra
    forfiles /p "%LOG_DIR%" /m velmore-rendimiento-*.log /d -15 /c "cmd /c del @path" 2>nul
    echo [OK] Logs antiguos de rendimiento depurados.
)

echo [INFO] Eliminando compilaciones temporales...
if exist "%TARGET_DIR%\classes" (
    rmdir /s /q "%TARGET_DIR%\classes"
    echo [OK] Carpeta classes/ eliminada.
)

echo [INFO] Mantenimiento completado.
echo ==============================================
pause
