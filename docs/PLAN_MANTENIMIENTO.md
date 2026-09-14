# Plan de Mantenimiento Integral — Proyecto VELMORE
**Taller de Mantenimiento | Nivel de Avance: 100%**  
*Stack: Spring Boot 3.2.5 + Cron + MySQL + Batch*

---

## 1. Visión General
El ciclo de vida del software requiere operaciones constantes para preservar el rendimiento, asegurar la persistencia de datos y evitar problemas de estancamiento. Este Plan Integral define las rutinas automáticas dentro de la aplicación (Cron Jobs) y fuera de ella (Scripts OS) que el equipo de Operaciones debe supervisar.

---

## 2. Tareas Programadas Internas (Cron Jobs)

Spring Boot gestiona tareas asíncronas con el motor `@Scheduled` habilitado en `VelmoreLibreriasApplication.java`. Estas operan de forma transparente y sin downtime.

### 2.1 Refresco de Caché (`rutinaMadrugada`)
* **Frecuencia**: Diario, 03:00 AM (Hora del servidor).
* **Impacto**: Bajo. La aplicación no entra en downtime.
* **Propósito**: Ejecuta `productoServiceImpl.limpiarCaches()`. Vacía las memorias locales de Google Guava (`cachePorId` y `cacheCatalogo`). 
* **Justificación**: Mitiga fugas de memoria silenciosas (memory leaks) o el uso excesivo del *Heap* acumulado durante las consultas pesadas del día. Al comenzar la jornada, el caché se reconstruye limpiamente en los primeros hits.

### 2.2 Reporte Ejecutivo Matutino (`reporteMatutino`)
* **Frecuencia**: Diario, 08:00 AM.
* **Propósito**: Imprime en los logs (y, por consiguiente, en `velmore.log` y sistemas de indexación si existen) una "foto" del inventario.
* **Métricas**: Productos activos, valor referencial del inventario en S/, tamaño inicial del caché.

---

## 3. Scripts de Administración y Mantenimiento Manual

En la carpeta `deploy/` existen scripts de DOS/Batch diseñados para automatizar tareas complejas.

| Script | Descripción y Uso | Frecuencia Sugerida |
|--------|------------------|---------------------|
| `backup-db.bat` | Ejecuta `mysqldump` de `velmore_db` y guarda en `deploy/backups/velmore_backup_YYYY-MM-DD_HHMM.sql`. Usa variables de entorno `VELMORE_DB_USER` y `VELMORE_DB_PASS`. | **Diario** (Ejecutado por Programador de Tareas de Windows a las 02:00 AM). |
| `restore-db.bat <archivo>` | Restaura el volcado SQL. **¡Peligro!** Sobreescribe la BD actual. Solicita confirmación explícita (Escribir "SI"). | Solo en caso de **Disaster Recovery**. |
| `limpiar-temp.bat` | Elimina binarios antiguos (`target/classes/`) y purga logs de rendimiento con > 15 días de antigüedad. | Semanal o cuando el disco > 85% de uso. |

---

## 4. Política de Retención y Backups

### 4.1 Retención de Logs (Controlado por Logback)
- **App_File (`velmore.log`)**: Se retiene por **7 días**. Tamaño total no mayor a 100MB.
- **Alertas (`velmore-alertas.log`)**: Se retiene por **30 días**. Vital para análisis de problemas. Tamaño max: 200MB.

### 4.2 Retención de Backups de Base de Datos
- **Medio**: Local en `deploy/backups/`.
- **Política RPO/RTO**: El RPO (Recovery Point Objective) es de 24 horas. El RTO (Recovery Time Objective) es de ~5 minutos (ejecutando `restore-db.bat`).
- **Mantenimiento**: Cada fin de mes, el operador debe archivar los backups del mes anterior en almacenamiento frío (S3 o disco externo) y purgar la carpeta local para no saturar el servidor.

---

## 5. Procedimientos de Respuesta ante Incidentes

### 5.1 Caso A: Corrupción de datos o ataque Ransomware a la BD
1. Bajar el servicio (`Ctrl+C` en la ventana del servidor `run-prod.bat`).
2. Abrir consola en `deploy/`.
3. Revisar el archivo más reciente en la carpeta `backups/`.
4. Ejecutar: `restore-db.bat backups\velmore_backup_XXXX.sql`
5. Levantar el servicio: `run-prod.bat`.
6. Informar del downtime y revisar logs de seguridad.

### 5.2 Caso B: Uso de CPU al 100% o falta de RAM (`OutOfMemoryError`)
1. Generar volcado de memoria si es posible.
2. Reiniciar el servidor (`run-prod.bat`).
3. (Si no es horario pico) Ejecutar el script HTTP `GET /actuator/health/guavacache` para descartar saturación.
4. Ajustar el heap size de la JVM en el archivo `run-prod.bat` (Actualmente `-Xmx1024m`, subir a `-Xmx2048m` si la máquina tiene RAM).

### 5.3 Caso C: Problemas con el arranque (Puerto 8080/8081 en uso)
1. Abrir CMD como Administrador: `netstat -ano | findstr :8080`
2. Matar el proceso huérfano: `taskkill /PID <NUMERO_PID> /F`

---
*Documento de Mantenimiento Integral - Versión 1.0 (Final)*  
*Cierre de Proyecto 100% - VELMORE*
