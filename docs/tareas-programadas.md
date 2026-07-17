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
| `TipoTareaProgramada` | Enum que identifica cada tarea (`REPORTE_SEMANAL_RESERVAS`, `LIMPIEZA_LOGS_EMAIL`, `CANCELACION_AUTOMATICA_RESERVAS`). |
| `EstadoEjecucionTarea` | Resultado de la corrida (`EXITO` / `FALLIDO`). |
| `LogTareaProgramada` | Entity de la tabla `log_tareas_programadas`. |
| `LogTareaProgramadaRepository` | Acceso a la tabla de logs. |
| `LogTareaProgramadaRegistrar` | Persiste el log en una transacción propia (`REQUIRES_NEW`). |

### Tareas concretas

| Scheduler | Service | Tarea |
|---|---|---|
| `reservas/scheduled/ReporteSemanalReservasScheduler` | `ReporteSemanalReservasService` | Mail con las reservas de la semana. |
| `shared/mantenimiento/LimpiezaLogsEmailScheduler` | `LimpiezaLogsEmailService` | Purga de `envio_emails_logs` viejos. |
| `reservas/scheduled/CancelacionAutomaticaReservasScheduler` | `CancelacionAutomaticaReservasService` | Cancela reservas `PENDIENTE` con plazo de confirmación vencido. |
| `reservas/scheduled/TransicionEstadoReservasPorFechaScheduler` | `TransicionEstadoReservasPorFechaService` | Pasa reservas a EN_CURSO/FINALIZADA/VENCIDA_SIN_PAGO según la fecha. |
| `clientes/scheduled/InactivacionSociosScheduler` | `InactivacionSociosService` | Inactivación automática de socios morosos. |

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

---

## Las garantías del diseño

- El registro de **ÉXITO** se hace fuera del `try` de la ejecución: si la tarea corrió bien
  pero falló el guardado del log, no queda marcada como `FALLIDO`.
- El registro del log en sí (`registrarSeguro`) está contenido: si persistir el log falla,
  se loguea el error pero no se relanza, para que el hilo del scheduler siga vivo.
- Una excepción de la tarea **nunca** se propaga fuera de `EjecutorTareaProgramada.ejecutar()`.

---

## La tabla de logs

`log_tareas_programadas` guarda cada corrida: tipo de tarea, estado (`EXITO`/`FALLIDO`),
inicio, fin, resumen (si tuvo éxito) o detalle del error (si falló). Tiene un índice sobre
`(tarea, inicio)` para consultar el historial de una tarea puntual.

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

# Cancelacion automatica de reservas pendientes vencidas: cada hora en punto (hora de Uruguay).
cipolflo.tareas.cancelacion-automatica-reservas.cron=0 0 * * * *
cipolflo.tareas.cancelacion-automatica-reservas.zona=America/Montevideo
# Transicion de estados de reservas por fecha: todos los dias 00:00 (hora de Uruguay).
cipolflo.tareas.transicion-estado-reservas.cron=0 0 0 * * *
cipolflo.tareas.transicion-estado-reservas.zona=America/Montevideo

# Inactivación automática de socios morosos: primer día de cada mes 03:00 (hora de Uruguay).
cipolflo.tareas.inactivacion-socios.cron=0 0 3 1 * *
cipolflo.tareas.inactivacion-socios.zona=America/Montevideo
```

Cada tarea bindea sus properties con un record `@ConfigurationProperties` (patrón
`MailProperties`): `ReporteSemanalReservasProperties`, `LimpiezaLogsEmailProperties`.

> **Formato del cron de Spring**: 6 campos — `segundo minuto hora día-mes mes día-semana`.
> Ej. `0 0 8 * * MON` = todos los lunes a las 08:00:00. Ej. `0 0 * * * *` = todas las horas
> en punto.

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

### Cancelación automática de reservas vencidas

Corre **cada hora en punto** (hora de Uruguay) y cancela las reservas en estado `PENDIENTE`
cuya `fechaLimiteConfirmacion` ya pasó. Esa fecha se deriva del `plazoConfirmacion` elegido
al crear la reserva (ver el contrato de API):

| Plazo | Se cancela | Ventana de alerta previa |
|---|---|---|
| `VEINTICUATRO_HORAS` | inicio − 24 h | 24 h antes del límite |
| `TRES_MESES` | inicio − 3 meses | 7 días antes del límite |

El guard de estado (`PENDIENTE`) vive en la query del repositorio
(`findByEstadoAndFechaLimiteConfirmacionLessThanEqual`), no en memoria: así el job nunca
puede tocar una reserva `CONFIRMADA`, `EN_CURSO`, `FINALIZADA` ni `CANCELADA`. Cancela
directo con `reserva.cancelar()` + `saveAll` — **no** pasa por el flujo de cancelación
manual (`CancelacionReservaService`), que exige una decisión de devolución que solo puede
tomar un humano.

Es idempotente: al cancelar, la reserva sale del predicado de la query, así que una segunda
corrida no la vuelve a tocar. Reservas sin `plazoConfirmacion` (no requieren seña ni
documentación) nunca entran en este flujo, porque su `fechaLimiteConfirmacion` es `null`.

Con un job diario un plazo de 24 hs se cancelaría hasta ~24 hs tarde; por eso el cron es
horario en vez de diario, a diferencia de las otras dos tareas de esta lista.
### Transición de estados de reservas por fecha

Corre **todos los días a las 00:00** (hora de Uruguay) y mueve reservas de estado según
la fecha de hoy:

1. **CONFIRMADA → EN_CURSO**: reservas cuya `fechaEntrada` es hoy.
2. **EN_CURSO → FINALIZADA** (si `estaPaga()`) **o VENCIDA_SIN_PAGO** (si no): reservas
   cuya `fechaSalida` fue ayer, es decir que terminaron el día anterior a que corre la
   tarea.

`VENCIDA_SIN_PAGO` es finalizable manualmente (`esFinalizable()` la incluye junto con
`EN_CURSO`), pero no cuenta como estado "ocupante" del servicio (`esOcupante()`): al
llegar a ese estado la estadía ya terminó, solo falta cobrar.

### Inactivación automática de socios

El **primer día de cada mes a las 03:00** evalúa a todos los socios `ACTIVO`. Para cada uno,
recalcula desde cero (consultando `pago_cuota`) cuántos meses **completos** adeuda: compara
el primer período pendiente (mes siguiente al último pagado, o `fechaIngreso` si nunca pagó)
contra el mes en curso. El mes en curso nunca cuenta como adeudado — recién empieza, todavía
no venció. Si el resultado llega a 3, pasa al socio a `INACTIVO` vía
`Socio.pasarAInactivoPorMorosidad()` (ver [Patrones de dominio en `Socio`](../AGENTS.md)). Un
socio que adeuda menos de 3 meses no se toca.

> Al no depender de un contador persistido sino recalcular todo en cada corrida, es
> idempotente: una corrida salteada no descuadra el resultado de la siguiente, y un socio que
> se puso al día simplemente da 0 la próxima vez.

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