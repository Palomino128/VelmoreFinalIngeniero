@echo off
REM ================================================================
REM  VELMORE — Script de despliegue PRODUCCION
REM  Requiere: Java 17+, MySQL remoto accesible
REM
REM  ANTES DE EJECUTAR — configurar variables de entorno:
REM    SET VELMORE_DB_URL=jdbc:mysql://servidor:3306/velmore_db?useSSL=true...
REM    SET VELMORE_DB_USER=usuario_bd
REM    SET VELMORE_DB_PASS=contraseña_bd
REM    SET VELMORE_ADMIN_USER=admin
REM    SET VELMORE_ADMIN_PASS=ContraseñaSegura2024!
REM    SET VELMORE_WA_NUMERO=51999999999
REM    SET SERVER_PORT=8080
REM
REM  O cargar desde un archivo .env:
REM    for /F "tokens=*" %%A in (.env) do SET %%A
REM ================================================================

SETLOCAL

REM ── Configuración base ──────────────────────────────────────────
SET APP_NAME=VELMORE Librería de Perfumes
SET JAR_FILE=..\target\velmore-librerias-final-1.0.0.jar
SET PROFILE=prod
SET LOG_DIR=..\logs

REM ── JVM optimizada para producción ──────────────────────────────
REM   -Xms512m        : Memoria inicial de heap
REM   -Xmx1024m       : Memoria máxima de heap
REM   -XX:+UseG1GC    : Garbage Collector G1 (equilibrado)
REM   -Dfile.encoding : Soporte UTF-8 para caracteres especiales
SET JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -Dfile.encoding=UTF-8

REM ── Usar puerto de variable de entorno o 8080 por defecto ───────
IF "%SERVER_PORT%"=="" SET SERVER_PORT=8080

REM ── Banner ──────────────────────────────────────────────────────
echo.
echo ================================================================
echo   %APP_NAME%  ^|  Entorno: PRODUCCION
echo ================================================================
echo.

REM ── Verificar Java ──────────────────────────────────────────────
java -version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java no encontrado. Instale Java 17+ en el servidor.
    EXIT /B 1
)
echo [INFO] Java OK

REM ── Verificar JAR ───────────────────────────────────────────────
IF NOT EXIST "%JAR_FILE%" (
    echo [ERROR] JAR no encontrado: %JAR_FILE%
    echo         Ejecute: mvn clean package -DskipTests -Pprod
    EXIT /B 1
)
echo [INFO] JAR: %JAR_FILE%

REM ── Verificar variables de entorno críticas ─────────────────────
IF "%VELMORE_DB_PASS%"=="" (
    echo [WARN] Variable VELMORE_DB_PASS no definida. Usando valor por defecto.
    echo        SEGURIDAD: En produccion siempre defina esta variable.
)
IF "%VELMORE_ADMIN_PASS%"=="" (
    echo [WARN] Variable VELMORE_ADMIN_PASS no definida. Usando valor por defecto.
)

REM ── Crear directorio de logs ─────────────────────────────────────
IF NOT EXIST "%LOG_DIR%" (
    mkdir "%LOG_DIR%"
    echo [INFO] Directorio de logs creado: %LOG_DIR%
)

REM ── Arrancar aplicacion ─────────────────────────────────────────
echo.
echo [INFO] Iniciando VELMORE en modo PRODUCCION...
echo [INFO] Puerto: %SERVER_PORT%
echo [INFO] URL: http://localhost:%SERVER_PORT%
echo [INFO] Health: http://localhost:%SERVER_PORT%/actuator/health
echo [INFO] Logs: %LOG_DIR%\velmore.log
echo.
echo ================================================================
echo.

java %JAVA_OPTS% ^
    -jar "%JAR_FILE%" ^
    --spring.profiles.active=%PROFILE% ^
    --server.port=%SERVER_PORT%

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] La aplicacion termino con error. Revise: %LOG_DIR%\velmore.log
    EXIT /B 1
)

ENDLOCAL
