@echo off
setlocal
chcp 65001 >nul
:: ============================================================================
:: SCRIPT DE BACKUP DE BASE DE DATOS VELMORE
:: Taller de Mantenimiento
:: ============================================================================

echo ========================================
echo [VELMORE] Iniciando proceso de backup...
echo ========================================

:: 1. Definir variables (pueden venir del entorno o ser sobreescritas aqui)
if "%VELMORE_DB_USER%"=="" set VELMORE_DB_USER=root
if "%VELMORE_DB_PASS%"=="" set VELMORE_DB_PASS=
set DB_NAME=velmore_db
set BACKUP_DIR=backups

:: 2. Crear carpeta de backups si no existe
if not exist "%BACKUP_DIR%" (
    mkdir "%BACKUP_DIR%"
    echo [INFO] Carpeta de backups creada: %BACKUP_DIR%
)

:: 3. Generar nombre de archivo con timestamp
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c-%%a-%%b)
for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set mytime=%mytime: =0%
set FILENAME=%BACKUP_DIR%\velmore_backup_%mydate%_%mytime%.sql

:: 4. Ejecutar mysqldump (Requiere que mysql\bin este en el PATH, tipico en XAMPP)
echo [EXEC] Exportando %DB_NAME% a %FILENAME%...

if "%VELMORE_DB_PASS%"=="" (
    mysqldump -u %VELMORE_DB_USER% %DB_NAME% > "%FILENAME%"
) else (
    mysqldump -u %VELMORE_DB_USER% -p%VELMORE_DB_PASS% %DB_NAME% > "%FILENAME%"
)

if %ERRORLEVEL% equ 0 (
    echo [OK] Backup completado exitosamente.
    echo [INFO] Archivo guardado en: %FILENAME%
) else (
    echo [ERROR] Fallo al generar el backup. Verifique que mysqldump esta instalado y accesible.
)

echo ========================================
pause
