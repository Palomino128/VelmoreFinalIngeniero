@echo off
REM ================================================================
REM  VELMORE — Script de despliegue DESARROLLO
REM  Requiere: Java 17+, MySQL/XAMPP activo en puerto 3306
REM  Uso: doble clic en run-dev.bat  O  ejecutar desde CMD
REM ================================================================

SETLOCAL

REM ── Configuración ───────────────────────────────────────────────
SET APP_NAME=VELMORE Librería de Perfumes
SET JAR_FILE=..\target\velmore-librerias-final-1.0.0.jar
SET PROFILE=dev
SET PORT=8081
SET JAVA_OPTS=-Xms256m -Xmx512m -Dfile.encoding=UTF-8

REM ── Verificar Java ──────────────────────────────────────────────
echo.
echo ================================================================
echo   %APP_NAME%  ^|  Entorno: DESARROLLO
echo ================================================================
echo.

java -version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java no encontrado en el PATH.
    echo         Instale Java 17+ y agregue al PATH del sistema.
    echo         Descarga: https://adoptium.net/
    pause
    EXIT /B 1
)

FOR /F "tokens=3" %%G IN ('java -version 2^>^&1 ^| findstr /i "version"') DO (
    SET JAVA_VERSION=%%G
)
echo [INFO] Java detectado: %JAVA_VERSION%

REM ── Verificar JAR ───────────────────────────────────────────────
IF NOT EXIST "%JAR_FILE%" (
    echo [ERROR] JAR no encontrado: %JAR_FILE%
    echo         Ejecute primero: mvn clean package -DskipTests
    pause
    EXIT /B 1
)
echo [INFO] JAR encontrado: %JAR_FILE%

REM ── Verificar MySQL ─────────────────────────────────────────────
echo [INFO] Verificando conexion MySQL en localhost:3306...
powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient('localhost', 3306); $tcp.Close(); Write-Host '[INFO] MySQL activo en puerto 3306' } catch { Write-Host '[WARN] MySQL no responde. Asegurese de que XAMPP este activo.' }"

REM ── Crear directorio de logs ─────────────────────────────────────
IF NOT EXIST "..\logs" (
    mkdir "..\logs"
    echo [INFO] Directorio de logs creado: ..\logs\
)

REM ── Arrancar aplicacion ─────────────────────────────────────────
echo.
echo [INFO] Iniciando VELMORE en modo DESARROLLO...
echo [INFO] URL: http://localhost:%PORT%
echo [INFO] Health: http://localhost:%PORT%/actuator/health
echo [INFO] Admin: http://localhost:%PORT%/admin
echo [INFO] Credenciales: admin / Velmore2024$
echo [INFO] Logs: ..\logs\velmore.log
echo.
echo [INFO] Presione Ctrl+C para detener la aplicacion.
echo ================================================================
echo.

java %JAVA_OPTS% -jar "%JAR_FILE%" --spring.profiles.active=%PROFILE% --server.port=%PORT%

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] La aplicacion termino con error. Revise los logs en: ..\logs\velmore.log
    pause
)

ENDLOCAL
