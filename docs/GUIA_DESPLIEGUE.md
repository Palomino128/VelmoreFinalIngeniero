# Guía de Despliegue — Proyecto VELMORE
**Taller de Despliegue | Avance: 80%**

---

## 1. Descripción del Proyecto

| Atributo | Valor |
|----------|-------|
| **Aplicación** | VELMORE — Sistema de Gestión de Catálogo de Perfumes |
| **Versión** | 1.0.0 |
| **Framework** | Spring Boot 3.2.5 |
| **Lenguaje** | Java 17 |
| **Servidor embebido** | Apache Tomcat (Spring Boot) |
| **Base de datos** | MySQL 8 |
| **Empaquetado** | JAR ejecutable (Fat JAR con todas las dependencias) |
| **Puerto por defecto** | 8081 (dev) / 8080 (prod) |

---

## 2. Requisitos del Servidor

### Software requerido

| Componente | Versión mínima | Verificar con |
|------------|---------------|---------------|
| **Java JDK** | 17 | `java -version` |
| **MySQL** | 8.0 | `mysql --version` |
| **Maven** | 3.8+ | `mvn -version` |

### Recursos de hardware (mínimos para producción)
- **CPU**: 2 núcleos
- **RAM**: 1 GB disponible (la JVM usa entre 512 MB y 1 GB)
- **Disco**: 500 MB libres (JAR: 71 MB + logs + BD)
- **Red**: Acceso a puerto 8080 (o 8081 en desarrollo)

### Configuración de red
- Abrir puerto `8081` en firewall para desarrollo
- Abrir puerto `8080` en firewall para producción
- Endpoint de salud accesible: `/actuator/health`

---

## 3. Estructura del Proyecto Maven

```
VelmoreFinalIngeniero_FIXED/
├── pom.xml                           ← Descriptor del proyecto Maven
├── src/
│   ├── main/
│   │   ├── java/pe/edu/velmore/      ← Código fuente Java
│   │   └── resources/
│   │       ├── application.properties         ← Config. base (perfil dev activo)
│   │       ├── application-dev.properties     ← Perfil desarrollo (XAMPP)
│   │       ├── application-prod.properties    ← Perfil producción (servidor)
│   │       ├── data.sql                       ← Datos iniciales
│   │       ├── logback-spring.xml             ← Configuración de logging
│   │       ├── static/                        ← CSS, JS, imágenes
│   │       └── templates/                     ← Vistas Thymeleaf
│   └── test/                         ← Pruebas unitarias (75 tests)
├── deploy/
│   ├── run-dev.bat                   ← Script de arranque (desarrollo)
│   ├── run-prod.bat                  ← Script de arranque (producción)
│   └── .env.example                  ← Plantilla de variables de entorno
├── logs/                             ← Archivos de log (generados en runtime)
└── target/
    └── velmore-librerias-final-1.0.0.jar  ← JAR ejecutable (generado por Maven)
```

---

## 4. Ciclo de Vida Maven — Fases de Despliegue

```
clean → validate → compile → test → package → verify → install → deploy
```

### Comandos utilizados en este proyecto

| Fase | Comando | Descripción |
|------|---------|-------------|
| **Limpiar** | `mvn clean` | Elimina el directorio `target/` |
| **Compilar** | `mvn compile` | Compila los fuentes Java |
| **Probar** | `mvn test` | Ejecuta los 75 tests unitarios |
| **Empaquetar** | `mvn package -DskipTests` | Genera el JAR ejecutable |
| **Todo** | `mvn clean package -DskipTests` | Limpia y genera el JAR |

---

## 5. Configuración de la Base de Datos MySQL

### 5.1 Crear base de datos (XAMPP / MySQL local)

```sql
-- Conectarse a MySQL como root
CREATE DATABASE IF NOT EXISTS velmore_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Para producción: crear usuario dedicado (NO usar root)
CREATE USER 'velmore_user'@'localhost' IDENTIFIED BY 'MiContraseñaSegura2024!';
GRANT ALL PRIVILEGES ON velmore_db.* TO 'velmore_user'@'localhost';
FLUSH PRIVILEGES;
```

### 5.2 Inicialización automática de tablas
Spring Boot con `spring.jpa.hibernate.ddl-auto=update` crea automáticamente
la tabla `productos` al arrancar la primera vez.

El archivo `data.sql` inserta los **8 productos iniciales** automáticamente:
```sql
INSERT IGNORE INTO productos (...) VALUES
  (1, 'Khamrah', 'Lattafa', 'UNISEX', 169, ...),
  ...
  (8, 'Asad Bourbon', 'Lattafa', 'MASCULINO', 165, ...);
```

---

## 6. Generación del JAR con Maven

### 6.1 Comando de empaquetado

```powershell
# En NetBeans o desde CMD/PowerShell:
$env:JAVA_HOME = "C:\Program Files\Apache NetBeans\jdk"
& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" clean package -DskipTests
```

### 6.2 Resultado esperado

```
[INFO] BUILD SUCCESS
[INFO] Building jar: target/velmore-librerias-final-1.0.0.jar
```

El JAR generado es un **Fat JAR** (JAR ejecutable autocontenido) que incluye:
- El código compilado del proyecto VELMORE
- Todas las dependencias: Spring Boot, Guava, Apache POI, Commons, etc.
- El servidor Tomcat embebido
- Los recursos estáticos y plantillas Thymeleaf

**Tamaño**: ~71 MB

### 6.3 Verificar el MANIFEST.MF del JAR

```powershell
# Inspeccionar el manifiesto del JAR:
& java -jar target/velmore-librerias-final-1.0.0.jar --help 2>&1 | Select-Object -First 5
```

El manifiesto debe contener:
```
Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: pe.edu.velmore.VelmoreLibreriasApplication
Spring-Boot-Version: 3.2.5
```

---

## 7. Ejecución con Perfiles Spring Boot

### 7.1 Perfil de Desarrollo (XAMPP local)

```powershell
# Opción 1: Usando el script de despliegue
deploy\run-dev.bat

# Opción 2: Comando directo
java -Xms256m -Xmx512m -jar target/velmore-librerias-final-1.0.0.jar ^
     --spring.profiles.active=dev ^
     --server.port=8081
```

**Características del perfil dev:**
- Puerto: `8081`
- SQL visible en consola (debug)
- Cache de Thymeleaf deshabilitado (cambios en vivo)
- Logging nivel DEBUG
- `ddl-auto=update` (actualiza tablas automáticamente)

### 7.2 Perfil de Producción (servidor remoto)

```powershell
# 1. Configurar variables de entorno
SET VELMORE_DB_URL=jdbc:mysql://servidor:3306/velmore_db?useSSL=true
SET VELMORE_DB_USER=velmore_user
SET VELMORE_DB_PASS=ContraseñaSegura!
SET VELMORE_ADMIN_PASS=AdminPass2024!

# 2. Usar el script de despliegue
deploy\run-prod.bat

# O comando directo con JVM optimizada:
java -Xms512m -Xmx1024m -XX:+UseG1GC -Dfile.encoding=UTF-8 ^
     -jar target/velmore-librerias-final-1.0.0.jar ^
     --spring.profiles.active=prod ^
     --server.port=8080
```

**Características del perfil prod:**
- Puerto: `8080`
- SQL oculto (performance + seguridad)
- Cache de Thymeleaf habilitado
- Logging nivel WARN (solo errores importantes)
- `ddl-auto=validate` (no modifica el esquema)
- Todos los valores sensibles desde variables de entorno

---

## 8. Verificación del Despliegue

### 8.1 Spring Boot Actuator — Health Check

Una vez iniciada la aplicación, verificar con:

```powershell
# Verificar que la aplicación está "viva"
Invoke-WebRequest -Uri http://localhost:8081/actuator/health

# Respuesta esperada:
# {"status":"UP","components":{"db":{"status":"UP"},...}}
```

| Endpoint | URL | Descripción |
|----------|-----|-------------|
| **Health** | `/actuator/health` | Estado general (UP/DOWN) |
| **Info** | `/actuator/info` | Versión, nombre, tecnologías |
| **Metrics** | `/actuator/metrics` | Métricas de la JVM |

### 8.2 Verificación de la aplicación

```
URL Base:    http://localhost:8081
Catálogo:    http://localhost:8081/catalogo
Login admin: http://localhost:8081/login
Panel admin: http://localhost:8081/admin
Health:      http://localhost:8081/actuator/health
Info:        http://localhost:8081/actuator/info
```

### 8.3 Credenciales de acceso

| Perfil | Usuario | Contraseña |
|--------|---------|-----------|
| Admin | `admin` | `Velmore2024$` (dev) / variable de entorno (prod) |
| Cliente | `cliente` | `123456` |

---

## 9. Monitoreo con Logs

### 9.1 Ubicación de logs

```
logs/
├── velmore.log               ← Log del día actual
└── velmore-2026-06-25.log   ← Logs rotados (7 días de historial)
```

### 9.2 Configuración de Logback

El archivo `logback-spring.xml` configura:
- **Appender CONSOLE**: Salida en pantalla con formato: `timestamp LEVEL logger - mensaje`
- **Appender FILE**: Archivo rotado diariamente, retención de **7 días**
- **Nivel raíz**: INFO (dev: DEBUG, prod: WARN)

### 9.3 Comandos para monitorear logs en tiempo real

```powershell
# Windows — ver los últimos 20 registros y seguir en vivo:
Get-Content logs\velmore.log -Tail 20 -Wait

# Filtrar solo errores:
Get-Content logs\velmore.log | Select-String "ERROR"

# Filtrar eventos de caché Guava:
Get-Content logs\velmore.log | Select-String "CACHE"
```

---

## 10. Observaciones Levantadas y Mejoras Aplicadas

Durante el proceso de despliegue se identificaron y aplicaron las siguientes mejoras:

| # | Observación | Mejora Aplicada |
|---|-------------|-----------------|
| OBS-01 | Sin perfil de producción separado | ✅ Creado `application-prod.properties` con configuración segura |
| OBS-02 | Sin Health Check para monitoreo | ✅ Spring Boot Actuator integrado (`/actuator/health`) |
| OBS-03 | Scripts de arranque manuales | ✅ `run-dev.bat` y `run-prod.bat` con verificaciones automáticas |
| OBS-04 | Credenciales en texto en properties | ✅ Variables de entorno con `.env.example` como guía |
| OBS-05 | Sin versión explícita del compilador | ✅ `maven-compiler-plugin` configurado con Java 17 explícito |
| OBS-06 | Pool de conexiones no optimizado | ✅ HikariCP configurado: 5 conexiones (dev) / 20 (prod) |
| OBS-07 | Cache Thymeleaf siempre activo | ✅ Deshabilitado en dev, habilitado en prod |
| OBS-08 | `ddl-auto=update` en producción | ✅ Prod usa `validate` (más seguro, no altera el esquema) |

---

## 11. Flujo Completo de Despliegue

```
┌─────────────────────────────────────────────────────────────┐
│                  FLUJO DE DESPLIEGUE VELMORE                │
└─────────────────────────────────────────────────────────────┘

  Desarrollo                           Producción
  ──────────                           ──────────
  1. Editar código                     1. mvn clean package -DskipTests
  2. mvn test (75 tests)               2. Copiar JAR al servidor
  3. mvn package -DskipTests           3. Configurar variables de entorno
  4. deploy\run-dev.bat                4. deploy\run-prod.bat
  5. http://localhost:8081             5. GET /actuator/health → UP
  6. Verificar funcionalidad           6. Verificar http://servidor:8080
  7. Revisar logs/velmore.log          7. Monitorear logs en producción
```

---

## 12. Solución de Problemas Comunes

### ❌ "Port already in use" (Puerto ocupado)
```powershell
# Encontrar qué proceso usa el puerto 8081:
netstat -ano | findstr :8081
# Terminar el proceso:
taskkill /PID <PID> /F
```

### ❌ "Unable to connect to database" (Error BD)
1. Verificar que XAMPP/MySQL está activo
2. Confirmar que la BD `velmore_db` existe: `SHOW DATABASES;`
3. Verificar credenciales en `application-dev.properties`
4. Revisar logs: `Get-Content logs\velmore.log | Select-String "ERROR"`

### ❌ "Failed to start Spring Boot" (Error de arranque)
```powershell
# Ver el stack trace completo en los logs:
Get-Content logs\velmore.log -Tail 50
```

### ❌ "BUILD FAILURE: Could not find artifact" (Error Maven)
```powershell
# Limpiar caché de Maven y re-descargar dependencias:
& mvn.cmd dependency:resolve
# O forzar actualización:
& mvn.cmd clean package -DskipTests -U
```

### ❌ Actuator retorna 404
- Verificar que `spring-boot-starter-actuator` está en `pom.xml`
- Confirmar que `management.endpoints.web.exposure.include=health` está configurado
- El endpoint es: `http://localhost:8081/actuator/health` (no `/health`)

---

*Guía de Despliegue elaborada para el Taller de Despliegue — Proyecto VELMORE*  
*Stack: Spring Boot 3.2.5 + Maven + MySQL + Apache Tomcat embebido*
