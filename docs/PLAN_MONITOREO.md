# Plan de Monitoreo — Proyecto VELMORE
**Taller de Monitoreo | Avance: 90%**  
*Stack: Spring Boot 3.2.5 + Logback + Spring Actuator + Micrometer + Guava*

---

## 1. Introducción

El plan de monitoreo de VELMORE establece las **4 capas de observabilidad** implementadas
para garantizar la disponibilidad, rendimiento y correcta operación del sistema de catálogo
de perfumes. Cubre logs, herramientas de salud, métricas de rendimiento y métricas de negocio.

---

## 2. Arquitectura de Monitoreo

```
┌─────────────────────────────────────────────────────────────────┐
│                  STACK DE MONITOREO VELMORE                     │
├──────────────────────────┬──────────────────────────────────────┤
│  CAPA                    │  IMPLEMENTACIÓN                      │
├──────────────────────────┼──────────────────────────────────────┤
│  1. LOGS                 │  Logback 4 appenders con perfiles    │
│  2. SALUD                │  Spring Actuator + GuavaCacheHealth  │
│  3. MÉTRICAS             │  Micrometer (Gauges + Counters)      │
│  4. RENDIMIENTO          │  RendimientoInterceptor HTTP          │
└──────────────────────────┴──────────────────────────────────────┘
```

---

## 3. CAPA 1 — Sistema de Logging (Logback)

### 3.1 Archivos de log generados

| Archivo | Contenido | Retención |
|---------|-----------|-----------|
| `logs/velmore.log` | Todos los eventos INFO+ de la app | 7 días |
| `logs/velmore-YYYY-MM-DD.log` | Rotación diaria del log general | 7 días |
| `logs/velmore-alertas.log` | Solo eventos WARN y ERROR | **30 días** |
| `logs/velmore-alertas-YYYY-MM-DD.log` | Rotación diaria de alertas | 30 días |
| `logs/velmore-rendimiento.log` | Latencia de cada HTTP request | 7 días |
| `logs/velmore-rendimiento-YYYY-MM-DD.log` | Rotación diaria de rendimiento | 7 días |

### 3.2 Formato del log

```
2026-06-25 10:52:15.234 INFO  [http-nio-8081-exec-1] p.e.v.s.ProductoServiceImpl - [CACHE HIT] Catálogo desde Guava.
2026-06-25 10:52:15.280 INFO  [http-nio-8081-exec-1] p.e.v.m.RendimientoInterceptor - [PERF] GET  /catalogo → 23ms | status=200
2026-06-25 10:52:16.100 WARN  [http-nio-8081-exec-2] p.e.v.m.RendimientoInterceptor - [PERF SLOW] GET /reporte/excel → 650ms | status=200
```

**Campos del patrón**: `timestamp | nivel | thread | clase (40 chars) | mensaje`

### 3.3 Niveles de log por componente

| Componente | Perfil dev | Perfil prod |
|------------|-----------|-------------|
| `pe.edu.velmore` (toda la app) | DEBUG | INFO |
| `RendimientoInterceptor` | INFO | INFO |
| `org.hibernate.SQL` | DEBUG | WARN |
| `org.springframework` | INFO | WARN |
| `com.zaxxer.hikari` | INFO | WARN |

### 3.4 Eventos clave registrados en log

| Etiqueta | Nivel | Descripción |
|----------|-------|-------------|
| `[CACHE HIT]` | INFO | Producto servido desde RAM (Guava) |
| `[CACHE MISS]` | INFO | Caché vacío → consulta a MySQL |
| `[CACHE STORE]` | INFO | Catálogo guardado en caché |
| `[DB+CACHE]` | INFO | Escritura persistida y caché sincronizada |
| `[PERF]` | INFO | Latencia normal (< 500ms) |
| `[PERF SLOW]` | WARN | Latencia alta (500–999ms) |
| `[PERF CRITICAL]` | WARN | Latencia crítica (≥ 1000ms) |
| `[MONITOR]` | INFO | Estadísticas de salud del caché |
| `[MÉTRICA]` | DEBUG | Incremento de contador de negocio |

---

## 4. CAPA 2 — Herramientas de Salud (Spring Actuator)

### 4.1 Endpoints de salud disponibles

```
GET http://localhost:8081/actuator/health
```
```json
{
  "status": "UP",
  "components": {
    "db":          { "status": "UP", "details": { "database": "MySQL", "result": 1 } },
    "guavacache":  { "status": "UP", "details": { ... } },
    "diskSpace":   { "status": "UP", "details": { ... } },
    "ping":        { "status": "UP" }
  }
}
```

### 4.2 Health Indicator personalizado: GuavaCacheHealthIndicator

**URL**: `GET /actuator/health/guavacache`

```json
{
  "status": "UP",
  "details": {
    "cachePorId.hitRate":     "75.3%",
    "cachePorId.hitCount":    1204,
    "cachePorId.missCount":   394,
    "cachePorId.loadCount":   394,
    "cachePorId.evictions":   12,
    "cachePorId.size":        8,
    "cachePorId.avgLoadMs":   "2.45 ms",
    "cacheCatalogo.hitRate":  "88.1%",
    "cacheCatalogo.hitCount": 476,
    "umbralHitRate":          "30%",
    "peticionesAnalizadas":   1598,
    "estado":                 "ÓPTIMO"
  }
}
```

**Lógica de semáforo:**
- `UP` — hit rate ≥ 30% o sistema recién arrancado (< 50 peticiones)
- `DOWN` — hit rate < 30% con más de 50 peticiones (ineficiencia del caché)

### 4.3 Health Indicator de base de datos

**URL**: `GET /actuator/health/db`
- Verifica conectividad con MySQL en `localhost:3306`
- Ejecuta una query de ping para validar el pool HikariCP
- Estado `DOWN` si la conexión falla

---

## 5. CAPA 3 — Métricas de Rendimiento y Negocio (Micrometer)

### 5.1 Listado completo de métricas VELMORE

**URL**: `GET /actuator/metrics`

| Métrica | Tipo | Descripción | Umbral alerta |
|---------|------|-------------|---------------|
| `velmore.productos.total` | Gauge | Productos activos en catálogo | < 1 → alerta |
| `velmore.inventario.valor` | Gauge | Valor total del inventario (S/) | — |
| `velmore.cache.hit.rate` | Gauge | Hit rate del caché Guava (%) | < 30% → alerta |
| `velmore.cache.size` | Gauge | Entradas en caché por ID | — |
| `velmore.busquedas.total` | Counter | Búsquedas realizadas | — |
| `velmore.ordenes.whatsapp` | Counter | Links WhatsApp generados | — |
| `velmore.detalle.visitas` | Counter | Visitas a páginas de detalle | — |

### 5.2 Métricas de infraestructura (Spring/JVM)

| Métrica | Descripción | Umbral alerta |
|---------|-------------|---------------|
| `jvm.memory.used` | Memoria heap usada | > 80% del max → alerta |
| `jvm.gc.pause` | Duración de Garbage Collection | > 200ms → revisar |
| `jvm.threads.live` | Threads activos de la JVM | > 200 → alerta |
| `hikaricp.connections.active` | Conexiones MySQL activas | = max → bottleneck |
| `hikaricp.connections.timeout` | Timeouts del pool | > 0 → alerta |
| `http.server.requests` | Latencia y conteo de requests HTTP | p99 > 1000ms → alerta |

### 5.3 Consultar una métrica específica

```powershell
# Total de productos activos
Invoke-RestMethod http://localhost:8081/actuator/metrics/velmore.productos.total

# Respuesta:
# { "name": "velmore.productos.total", "measurements": [{ "statistic": "VALUE", "value": 8.0 }] }

# Hit rate del caché Guava
Invoke-RestMethod http://localhost:8081/actuator/metrics/velmore.cache.hit.rate

# Memoria JVM usada
Invoke-RestMethod "http://localhost:8081/actuator/metrics/jvm.memory.used?tag=area:heap"

# Conexiones activas HikariCP
Invoke-RestMethod "http://localhost:8081/actuator/metrics/hikaricp.connections.active"
```

---

## 6. CAPA 4 — Monitoreo de Rendimiento HTTP

### 6.1 RendimientoInterceptor

Implementa `HandlerInterceptor` — mide latencia de cada petición automáticamente.

**Clasificación de latencias:**

| Rango | Etiqueta | Acción |
|-------|----------|--------|
| < 500ms | `[PERF]` INFO | Normal |
| 500–999ms | `[PERF SLOW]` WARN | Investigar si se repite |
| ≥ 1000ms | `[PERF CRITICAL]` WARN | Acción inmediata |

**Ejemplo de log de rendimiento real:**
```
10:52:15 INFO  [PERF]          GET    /                                   → 145ms  | status=200
10:52:15 INFO  [PERF]          GET    /catalogo                           → 23ms   | status=200
10:52:16 WARN  [PERF SLOW]     GET    /reporte/excel                      → 650ms  | status=200
10:52:18 INFO  [PERF]          POST   /contacto/mensaje                   → 45ms   | status=200
10:52:20 INFO  [PERF]          GET    /producto/1                         → 8ms    | status=200
```

### 6.2 Análisis de rendimiento con logs

```powershell
# Ver todas las peticiones lentas (WARN) en el día de hoy:
Get-Content logs\velmore-rendimiento.log | Select-String "PERF SLOW|PERF CRITICAL"

# Ver promedio de latencias por ruta (análisis manual):
Get-Content logs\velmore-rendimiento.log | Select-String "\[PERF\]"

# Ver peticiones fallidas (status 5xx):
Get-Content logs\velmore.log | Select-String "status=5"
```

---

## 7. KPIs y SLOs del Sistema

### 7.1 Objetivos de Nivel de Servicio (SLO)

| KPI | Objetivo | Medición |
|-----|----------|----------|
| **Disponibilidad** | 99.5% uptime | `/actuator/health` → UP |
| **Latencia P95** | < 300ms | `http.server.requests` percentil 95 |
| **Latencia P99** | < 1000ms | `http.server.requests` percentil 99 |
| **Cache Hit Rate** | ≥ 30% | `/actuator/health/guavacache` |
| **Errores HTTP 5xx** | < 1% del total de requests | Logs + `http.server.requests` |
| **Pool BD** | < 80% de conexiones usadas | `hikaricp.connections.active` |
| **Memoria JVM** | < 80% del heap máximo | `jvm.memory.used` |

### 7.2 Umbrales de Alerta

| Componente | Normal | Atención | Crítico |
|------------|--------|----------|---------|
| Latencia HTTP | < 300ms | 300–999ms | ≥ 1000ms |
| Cache Hit Rate | ≥ 50% | 30–49% | < 30% |
| Memoria JVM | < 60% | 60–79% | ≥ 80% |
| Conexiones BD | < 10/20 | 15–18/20 | = 20/20 |
| Errores 5xx | 0 | 1–5/hora | > 5/hora |

---

## 8. Proceso de Escalamiento de Incidentes

```
┌─────────────────────────────────────────────────────────────┐
│  NIVEL 1 — Detección automática (Logback + Actuator)       │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ Log WARN en velmore-alertas.log                     │    │
│  │ /actuator/health retorna DOWN                       │    │
│  └─────────────────────────────────────────────────────┘    │
│                          ↓                                  │
│  NIVEL 2 — Diagnóstico (comandos de monitoreo)             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ 1. GET /actuator/health (¿qué componente falló?)    │    │
│  │ 2. GET /actuator/metrics/velmore.cache.hit.rate     │    │
│  │ 3. Get-Content logs\velmore-alertas.log -Tail 50    │    │
│  │ 4. GET /actuator/metrics/hikaricp.connections       │    │
│  └─────────────────────────────────────────────────────┘    │
│                          ↓                                  │
│  NIVEL 3 — Resolución                                      │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ BD caída    → reiniciar XAMPP/MySQL                 │    │
│  │ OOM JVM     → reiniciar con más heap (-Xmx2048m)    │    │
│  │ Cache lento → verificar tamaño caché y expiración   │    │
│  │ App colgada → restart deploy\run-dev.bat            │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## 9. Dashboard — Comandos de Monitoreo Rápido

```powershell
# ─── SALUD GENERAL ──────────────────────────────────────────────────────────
# Estado completo del sistema
Invoke-RestMethod http://localhost:8081/actuator/health | ConvertTo-Json -Depth 5

# Solo el estado (UP/DOWN)
(Invoke-RestMethod http://localhost:8081/actuator/health).status

# ─── CACHÉ GUAVA ────────────────────────────────────────────────────────────
# Estado detallado del caché Guava
Invoke-RestMethod http://localhost:8081/actuator/health/guavacache | ConvertTo-Json -Depth 3

# Hit rate numérico
(Invoke-RestMethod http://localhost:8081/actuator/metrics/velmore.cache.hit.rate).measurements[0].value

# ─── MÉTRICAS DE NEGOCIO ────────────────────────────────────────────────────
# Total de productos activos
(Invoke-RestMethod http://localhost:8081/actuator/metrics/velmore.productos.total).measurements[0].value

# Valor del inventario
(Invoke-RestMethod http://localhost:8081/actuator/metrics/velmore.inventario.valor).measurements[0].value

# Búsquedas realizadas
(Invoke-RestMethod http://localhost:8081/actuator/metrics/velmore.busquedas.total).measurements[0].value

# Órdenes WhatsApp generadas
(Invoke-RestMethod http://localhost:8081/actuator/metrics/velmore.ordenes.whatsapp).measurements[0].value

# ─── JVM Y BASE DE DATOS ────────────────────────────────────────────────────
# Memoria heap usada (bytes)
(Invoke-RestMethod "http://localhost:8081/actuator/metrics/jvm.memory.used?tag=area:heap").measurements[0].value

# Conexiones activas en el pool
(Invoke-RestMethod http://localhost:8081/actuator/metrics/hikaricp.connections.active).measurements[0].value

# ─── LOGS EN TIEMPO REAL ────────────────────────────────────────────────────
# Log general en vivo:
Get-Content logs\velmore.log -Tail 20 -Wait

# Solo alertas (WARN/ERROR):
Get-Content logs\velmore-alertas.log -Tail 20 -Wait

# Solo rendimiento (latencias):
Get-Content logs\velmore-rendimiento.log -Tail 20 -Wait

# Peticiones lentas del día:
Get-Content logs\velmore-rendimiento.log | Select-String "PERF SLOW|PERF CRITICAL"

# Errores de la última hora:
Get-Content logs\velmore-alertas.log | Select-String "ERROR"
```

---

## 10. Resumen de Implementación

| Capa | Clase/Archivo | Endpoint/Archivo resultado |
|------|--------------|---------------------------|
| 1. Logs | [logback-spring.xml](file:///c:/Users/edupa/Downloads/VelmoreFinalIngeniero_DB_GUAVA%20(1)/VelmoreFinalIngeniero_FIXED/src/main/resources/logback-spring.xml) | `logs/velmore*.log`, `logs/velmore-alertas*.log`, `logs/velmore-rendimiento*.log` |
| 2. Salud | [GuavaCacheHealthIndicator.java](file:///c:/Users/edupa/Downloads/VelmoreFinalIngeniero_DB_GUAVA%20(1)/VelmoreFinalIngeniero_FIXED/src/main/java/pe/edu/velmore/monitor/GuavaCacheHealthIndicator.java) | `/actuator/health/guavacache` |
| 3. Métricas | [VelmoreMicrometerMetrics.java](file:///c:/Users/edupa/Downloads/VelmoreFinalIngeniero_DB_GUAVA%20(1)/VelmoreFinalIngeniero_FIXED/src/main/java/pe/edu/velmore/monitor/VelmoreMicrometerMetrics.java) | `/actuator/metrics/velmore.*` |
| 4. Rendimiento | [RendimientoInterceptor.java](file:///c:/Users/edupa/Downloads/VelmoreFinalIngeniero_DB_GUAVA%20(1)/VelmoreFinalIngeniero_FIXED/src/main/java/pe/edu/velmore/monitor/RendimientoInterceptor.java) | `logs/velmore-rendimiento.log` |

---

*Plan de Monitoreo elaborado para el Taller de Monitoreo — Proyecto VELMORE*
*Stack: Logback 1.4 + Spring Boot Actuator 3.2.5 + Micrometer 1.12 + Guava 33.2.1*
