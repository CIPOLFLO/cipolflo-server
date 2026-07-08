# Tareas Programadas — CIPOLFLO Server

> Cómo funcionan las tareas que corren solas en horarios fijos (reportes, limpiezas).
> Implementación basada en `@Scheduled` (cron) + un **ejecutor** común que cronometra,
> registra el resultado y contiene los errores.

---

## Índice

1. [Resumen](#resumen)
2. [Componentes](#componentes)
3. [Flujo end-to-end](#flujo-end-to-end)
4. [Las garantías del diseño](#las-garantías-del-diseño)
5. [La tabla de logs](#la-tabla-de-logs)
6. [Configuración](#configuración)
7. [Tareas implementadas](#tareas-implementadas)
8. [Cómo agregar una nueva tarea](#cómo-agregar-una-nueva-tarea)
9. [Notas](#notas)

---

## Resumen

Una tarea programada **no** mete su lógica dentro del `@Scheduled`. El patrón separa tres
responsabilidades:

- **Scheduler** (el *cuándo*): una cáscara delgada con `@Scheduled(cron=...)` que solo
  delega. No tiene lógica ni try/catch propio.
- **Service** (el *qué*): la lógica real. Devuelve un **resumen legible** de lo que hizo
  (ej. `"5 registros eliminados"`), que va al log.
- **Ejecutor** (el *cómo se registra*): `EjecutorTareaProgramada` envuelve la corrida del
  service, la cronometra, persiste el resultado y **atrapa cualquier excepción** para que
  una falla no rompa el hilo del scheduler ni impida futuras ejecuciones.

Así, agregar una tarea nueva es escribir un service que devuelve un resumen y un scheduler
de tres líneas; el trazado en base queda gratis.

---

## Componentes

### Infraestructura compartida (`shared/scheduling`, `shared/config`)

| Clase | Rol |
|---|---|
| `SchedulingConfig` | `@EnableScheduling`: habilita el procesamiento de `@Scheduled`. |
| `EjecutorTareaProgramada` | Corre una tarea (`Supplier<String>`), la cronometra, registra `EXITO`/`FALLIDO` y **no propaga** la excepción. |
| `TipoTareaProgramada` | Enum que identifica cada tarea (`REPORTE_SEMANAL_RESERVAS`, `LIMPIEZA_LOGS_EMAIL`, ...). |
| `EstadoEjecucionTarea` | Resultado de la corrida (`EXITO` / `FALLIDO`). |
| `LogTareaProgramada` | Entity de la tabla `log_tareas_programadas`. |
| `LogTareaProgramadaRepository` | Acceso a la tabla de logs. |
| `LogTareaProgramadaRegistrar` | Persiste el log en una transacción propia (`REQUIRES_NEW`). |

### Tareas concretas

| Scheduler | Service | Tarea |
|---|---|---|
| `reservas/scheduled/ReporteSemanalReservasScheduler` | `ReporteSemanalReservasService` | Mail con las reservas de la semana. |
| `shared/mantenimiento/LimpiezaLogsEmailScheduler` | `LimpiezaLogsEmailService` | Purga de `envio_emails_logs` viejos. |

---

## Flujo end-to-end

```
[cron dispara]  →  XxxScheduler.metodo()   [@Scheduled, hilo del scheduler]
                     └─ ejecutor.ejecutar(TipoTareaProgramada.XXX, service::hacerAlgo)
                              │
                              ▼
              EjecutorTareaProgramada.ejecutar()
                     ├─ inicio = now()
                     ├─ resumen = accion.get()        ← corre el service (el "qué")
                     │     ├─ ÉXITO → registrar(EXITO, inicio, fin, resumen, null)
                     │     └─ EXCEPCIÓN → registrar(FALLIDO, inicio, fin, null, error)   (no relanza)
                     └─ registrar(...)  [REQUIRES_NEW] → fila en log_tareas_programadas
```

### 1. El scheduler — el *cuándo*

```java
@Scheduled(
        cron = "${cipolflo.reportes.reservas-semanal.cron:-}",
        zone = "${cipolflo.reportes.reservas-semanal.zona:America/Montevideo}"
)
public void enviarReporteSemanal() {
    ejecutor.ejecutar(TipoTareaProgramada.REPORTE_SEMANAL_RESERVAS, reporteService::enviarReporteSemanal);
}
```

- **`cron`** y **`zone`** vienen de `application.properties` (no hardcodeados). Siempre se
  fija la zona (`America/Montevideo`) para que "lunes 08:00" sea hora local, no UTC.
- El default **`:-`** en el placeholder del cron es el valor especial de Spring que
  **deshabilita** el trigger. Sirve para que los contextos sin estas properties (ej. los
  tests) levanten sin fallar.
- El scheduler no tiene try/catch: eso lo hace el ejecutor.

### 2. El ejecutor — el *cómo se registra*

```java
public void ejecutar(TipoTareaProgramada tarea, Supplier<String> accion) {
    Instant inicio = Instant.now();
    try {
        String resumen = accion.get();
        registrar.registrar(tarea, EXITO, inicio, Instant.now(), resumen, null);
    } catch (Exception e) {
        String detalle = e.getClass().getSimpleName() + ": " + e.getMessage();
        registrar.registrar(tarea, FALLIDO, inicio, Instant.now(), null, detalle);
        // no relanza: la falla queda registrada, el scheduler sigue vivo
    }
}
```

### 3. El registrar — la persistencia del log

Bean aparte con **`@Transactional(propagation = REQUIRES_NEW)`**: transacción nueva e
independiente, de modo que el registro se commitea aunque la tarea haya hecho rollback.
Mismo patrón que `EnvioEmailLogRegistrar` en el subsistema de mails.

---

## Las garantías del diseño

1. **Una tarea que falla no tumba a las demás.** El ejecutor atrapa la excepción; el hilo
   del scheduler queda sano para la próxima corrida. (Por defecto Spring corre todas las
   `@Scheduled` en un único hilo.)
2. **Toda corrida queda registrada**, exitosa o fallida, en su propia transacción.
3. **Horarios en hora local** y configurables sin recompilar.
4. **Los tests no disparan tareas**: sin las properties de cron, el trigger queda
   deshabilitado (`-`).

---

## La tabla de logs

Tabla `log_tareas_programadas` (migración Liquibase `DEV-150`). Se inserta **una fila por
cada corrida**. Extiende `AuditableEntity` (aporta `created_at`, etc.).

| columna | tipo | descripción |
|---|---|---|
| `id` | bigserial PK | |
| `tarea` | varchar(50) | `REPORTE_SEMANAL_RESERVAS`, `LIMPIEZA_LOGS_EMAIL`, ... |
| `estado` | varchar(20) | `EXITO` / `FALLIDO` |
| `inicio` | timestamptz | cuándo arrancó |
| `fin` | timestamptz | cuándo terminó |
| `duracion_ms` | bigint | duración en milisegundos |
| `resumen` | text (nullable) | qué hizo (ej. `"3 en curso, 2 confirmadas, 1 pendiente"`) |
| `error` | text (nullable) | detalle del error cuando `estado = FALLIDO` |
| `created_at` / `updated_at` / `created_by` / `updated_by` | auditoría | |

Índice sobre `(tarea, inicio)` para consultar el historial de una tarea puntual.

---

## Configuración

En `application.properties`:

```properties
# Reporte semanal de reservas: lunes 08:00 (hora de Uruguay).
# Sin destinatario configurado, la tarea corre pero no envia (queda registrada como omitida).
cipolflo.reportes.reservas-semanal.cron=0 0 8 * * MON
cipolflo.reportes.reservas-semanal.zona=America/Montevideo
cipolflo.reportes.reservas-semanal.destinatario=${REPORTE_RESERVAS_DESTINATARIO:}

# Purga de logs de envio de emails: domingos 05:00 (hora de Uruguay).
cipolflo.tareas.limpieza-logs-email.cron=0 0 5 * * SUN
cipolflo.tareas.limpieza-logs-email.zona=America/Montevideo
cipolflo.tareas.limpieza-logs-email.retencion-dias=90
```

Cada tarea bindea sus properties con un record `@ConfigurationProperties` (patrón
`MailProperties`): `ReporteSemanalReservasProperties`, `LimpiezaLogsEmailProperties`.

> **Formato del cron de Spring**: 6 campos — `segundo minuto hora día-mes mes día-semana`.
> Ej. `0 0 8 * * MON` = todos los lunes a las 08:00:00.

---

## Tareas implementadas

### Reporte semanal de reservas

Los **lunes 08:00** arma un mail (texto plano) con las reservas de la semana que arranca
ese lunes (lunes a domingo). Muestra **nombre del servicio** y **nombre y teléfono del
cliente**, en tres secciones **disjuntas**:

1. **En curso** — `CONFIRMADA` o `EN_CURSO` que **empezaron antes** del lunes y siguen
   activas dentro de la semana (arrastre).
2. **Confirmadas** — `CONFIRMADA` que **inician** dentro de la semana.
3. **Pendientes** — `PENDIENTE` que **inician** dentro de la semana (arrancan pero aún no
   se confirmaron: falta documentación o falta pagar la seña).

Reusa el subsistema de mails (`IEmailService` + `SolicitudEmail` con
`TipoEventoEmail.REPORTE_SEMANAL_RESERVAS`), así que **cada envío también queda registrado
en `envio_emails_logs`** (ver [Envío de Emails](envio-emails.md)). Si el SMTP falla, la
`EmailException` se propaga y el ejecutor marca la corrida como `FALLIDO`.

> Reservas sin `clienteId` (caso temporal) se omiten por ahora.
> El destinatario es un único mail interno; si en el futuro son varios, cambiar
> `destinatario` a una lista.

### Purga de logs de email

Los **domingos 05:00** elimina de `envio_emails_logs` los registros con más de **90 días**
(configurable), usando `deleteByCreatedAtBefore` sobre el índice de `created_at`. Es
idempotente: si no hay vencidos, no borra nada.

---

## Cómo agregar una nueva tarea

Ejemplo: una limpieza de reservas finalizadas viejas.

1. **Agregar el valor** en `TipoTareaProgramada` (ej. `LIMPIEZA_RESERVAS_ANTIGUAS`).
2. **Crear el service** con la lógica. El método **devuelve un `String` resumen** de lo que
   hizo (para el log). Va anotado `@Transactional` si escribe/borra:

   ```java
   @Service
   public class LimpiezaReservasAntiguasService implements ILimpiezaReservasAntiguasService {
       @Override
       @Transactional
       public String purgar() {
           long borradas = ...;
           return borradas + " reservas antiguas eliminadas.";
       }
   }
   ```

3. **Crear las properties** (record `@ConfigurationProperties`) con al menos `cron` y `zona`,
   y agregarlas a `application.properties`. Cada tarea tiene su propio cron/horario.
4. **Crear el scheduler**, una cáscara que delega en el ejecutor:

   ```java
   @Component
   public class LimpiezaReservasAntiguasScheduler {
       private final ILimpiezaReservasAntiguasService service;
       private final EjecutorTareaProgramada ejecutor;
       // ...constructor...

       @Scheduled(
               cron = "${cipolflo.tareas.limpieza-reservas.cron:-}",
               zone = "${cipolflo.tareas.limpieza-reservas.zona:America/Montevideo}"
       )
       public void ejecutar() {
           ejecutor.ejecutar(TipoTareaProgramada.LIMPIEZA_RESERVAS_ANTIGUAS, service::purgar);
       }
   }
   ```

El cronometrado, el registro en `log_tareas_programadas` y la contención de errores quedan
**automáticos** por pasar por el ejecutor. Ubicá el scheduler y el service en el módulo de
su dominio (`reservas`, `clientes`, ...) o en `shared/mantenimiento` si es transversal.

> Recordá el default `:-` en el `cron` del placeholder para no romper los tests.

---

## Notas

- **Una sola instancia.** El diseño asume que corre una única instancia del server. Si en
  el futuro corren varias, todas dispararían la misma tarea a la vez; ahí habría que sumar
  un lock distribuido (ej. ShedLock). Hoy no está.
- **Pool de un hilo.** Spring ejecuta todas las `@Scheduled` en un único hilo. Con el
  volumen actual alcanza; si se agregan tareas pesadas o que se solapan, exponer un
  `TaskScheduler` con pool en `SchedulingConfig`.
