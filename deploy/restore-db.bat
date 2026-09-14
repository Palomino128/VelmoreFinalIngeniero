@echo off
setlocal
chcp 65001 >nul
:: ============================================================================
:: SCRIPT DE RESTAURACION DE BASE DE DATOS VELMORE
:: Taller de Mantenimiento
:: ============================================================================

echo ==============================================
echo [VELMORE] Asistente de Restauracion de Backup
echo ==============================================

if "%~1"=="" (
    echo [ERROR] Debe proporcionar el archivo de backup a restaurar.
    echo Uso: restore-db.bat backups\velmore_backup_fecha.sql
    goto end
)

if not exist "%~1" (
    echo [ERROR] El archivo '%~1' no existe.
    goto end
)

:: 1. Definir variables
if "%VELMORE_DB_USER%"=="" set VELMORE_DB_USER=root
if "%VELMORE_DB_PASS%"=="" set VELMORE_DB_PASS=
set DB_NAME=velmore_db

echo [WARN] ATENCION: Esta accion SOBRESCRIBIRA todos los datos en '%DB_NAME%'.
echo [WARN] Archivo de origen: %~1
set /p confirm=Escriba 'SI' para confirmar la restauracion: 

if /i "%confirm%" neq "SI" (
    echo [INFO] Restauracion cancelada por el usuario.
    goto end
)

echo [EXEC] Restaurando base de datos desde %~1...

if "%VELMORE_DB_PASS%"=="" (
    mysql -u %VELMORE_DB_USER% %DB_NAME% < "%~1"
) else (
    mysql -u %VELMORE_DB_USER% -p%VELMORE_DB_PASS% %DB_NAME% < "%~1"
)

if %ERRORLEVEL% equ 0 (
    echo [OK] Base de datos restaurada con exito.
) else (
    echo [ERROR] Fallo al restaurar la base de datos.
)

:end
echo ==============================================
pause
