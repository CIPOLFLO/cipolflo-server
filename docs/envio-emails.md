# Envío de Emails — CIPOLFLO Server

> Cómo funciona el envío de correos por eventos de negocio.
> Implementación basada en eventos de dominio + `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`.

---

## Índice

1. [Resumen](#resumen)
2. [Componentes](#componentes)
3. [Flujo end-to-end](#flujo-end-to-end)
4. [Las tres garantías del diseño](#las-tres-garantías-del-diseño)
5. [La tabla de logs](#la-tabla-de-logs)
6. [Configuración](#configuración)
7. [Cómo agregar un nuevo email](#cómo-agregar-un-nuevo-email)
8. [Notas de entorno local](#notas-de-entorno-local)

---

## Resumen

El envío de mails **no** se hace llamando al SMTP directamente dentro de la lógica de
negocio. En su lugar, el service de negocio **publica un evento** y un **listener**
separado se encarga de armar y enviar el correo, y de registrar el resultado.

Esto desacopla el negocio de la notificación: `ReservaService` no sabe nada de mails,
plantillas ni PDFs. El primer flujo implementado es el mail de **confirmación de reserva**.

---

## Componentes

### Infraestructura compartida (`shared/email`, `shared/config`)

| Clase | Rol |
|---|---|
| `AsyncConfig` | `@EnableAsync` + pool de hilos dedicado `mailExecutor`. |
| `IEmailService` / `EmailService` | Construye (con `MimeMessageHelper`) y envía el mail; registra el resultado. Soporta texto, HTML y adjuntos (PDFs). |
| `SolicitudEmail` | Record con todo lo necesario para enviar + el contexto de negocio (`tipoEvento`, `referenciaId`). |
| `EmailAdjunto` | Adjunto de un mail (nombre, bytes, contentType). |
| `MailProperties` | Remitente configurable (`cipolflo.mail.*`). |
| `TipoEventoEmail` | Enum del evento que originó el mail (`RESERVA_CREADA`, ...). |
| `EstadoEnvioEmail` | Resultado del intento (`ENVIADO` / `FALLIDO`). |
| `EnvioEmailLog` | Entity de la tabla `envio_emails_logs`. |
| `EnvioEmailLogRepository` | Acceso a la tabla de logs. |
| `EnvioEmailLogRegistrar` | Persiste el log en una transacción propia (`REQUIRES_NEW`). |
| `EmailException` | Envuelve los errores de bajo nivel en una API uniforme. |

### Flujo de reserva (`reservas/events`)

| Clase | Rol |
|---|---|
| `ReservaCreadaEvent` | Evento inmutable; lleva solo el `reservaId`. |
| `ReservaEmailListener` | Escucha el evento tras el commit, resuelve el email y envía. |

---

## Flujo end-to-end

```
POST /reservas  →  ReservaService.registrar()  [TRANSACCIÓN]
                     ├─ save(reserva)
                     └─ publishEvent(ReservaCreadaEvent(id))   ← solo "encola"
                   COMMIT ✅
                        │  (a partir de acá, en otro hilo)
                        ▼
        ReservaEmailListener.onReservaCreada()  [@Async, AFTER_COMMIT]
                     ├─ getDetalle(id) → email del cliente
                     ├─ ¿sin email? → log.debug y sale
                     └─ emailService.enviar(SolicitudEmail)
                              ├─ mailSender.send(...)  → SMTP (Gmail)
                              └─ logRegistrar.registrar(ENVIADO/FALLIDO)  [REQUIRES_NEW]
                                        → fila en envio_emails_logs
```

### 1. `ReservaService.registrar()` — el origen

Método `@Transactional`. Tras `reservaRepository.save(reserva)`, publica el evento:

```java
eventPublisher.publishEvent(new ReservaCreadaEvent(guardada.getId()));
```

`publishEvent` **no manda el mail**: solo anuncia que la reserva se creó, llevando
únicamente el `reservaId`. El service no conoce nada del subsistema de mails.

### 2. El commit (o rollback)

Cuando `registrar()` termina sin excepción, Spring hace **commit**. Ese momento decide
si el mail se envía o no.

### 3. `ReservaEmailListener.onReservaCreada()` — el consumidor

```java
@Async("mailExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onReservaCreada(ReservaCreadaEvent event) { ... }
```

- **`AFTER_COMMIT`**: corre **solo si el commit fue exitoso**. Si hubo rollback, el
  listener nunca se ejecuta.
- **`@Async("mailExecutor")`**: corre en un hilo aparte; la respuesta HTTP no espera al SMTP.

Adentro: recupera la reserva con `getDetalle(reservaId)`, obtiene el email del cliente
(si no tiene, loguea y sale), arma una `SolicitudEmail` con `TipoEventoEmail.RESERVA_CREADA`
y `referenciaId = reservaId`, y llama a `emailService.enviar(...)`. Si el envío falla,
captura la `EmailException` y la loguea **sin relanzar** (la reserva ya está guardada; el
mail es un efecto secundario).

### 4. `EmailService.enviar()` — el envío + el registro

Construye el `MimeMessage`, lo envía por SMTP y **siempre registra el resultado**:

```java
try {
    MimeMessage mensaje = construir(solicitud);
    mailSender.send(mensaje);
    logRegistrar.registrar(solicitud, ENVIADO, null);
} catch (EmailException e) {          // falló al construir
    logRegistrar.registrar(solicitud, FALLIDO, e.getMessage());
    throw e;
} catch (MailException e) {           // falló el envío SMTP
    logRegistrar.registrar(solicitud, FALLIDO, describir(e));
    throw new EmailException(...);
}
```

### 5. `EnvioEmailLogRegistrar.registrar()` — la persistencia del log

Bean aparte con **`@Transactional(propagation = REQUIRES_NEW)`**: transacción nueva e
independiente, de modo que el registro se commitea aunque el envío falle. Guarda una fila
en `envio_emails_logs`.

---

## Las tres garantías del diseño

1. **El mail se manda solo si la reserva se guardó de verdad** (`AFTER_COMMIT`).
2. **La respuesta HTTP no espera al SMTP** (`@Async`).
3. **Todo intento queda registrado**, exitoso o fallido, en una transacción propia.

---

## La tabla de logs

Tabla `envio_emails_logs` (migración Liquibase `DEV-148`). Se inserta **una fila por
cada intento** de envío. Extiende `AuditableEntity` (aporta `created_at`, etc.).

| columna | tipo | descripción |
|---|---|---|
| `id` | bigserial PK | |
| `destinatario` | varchar(255) | a quién se envió |
| `asunto` | varchar(255) | |
| `tipo_evento` | varchar(50) | `RESERVA_CREADA`, ... |
| `referencia_id` | bigint (nullable) | id de la entidad origen (ej. la reserva) |
| `estado` | varchar(20) | `ENVIADO` / `FALLIDO` |
| `error` | text (nullable) | detalle del error cuando falló |
| `created_at` / `updated_at` / `created_by` / `updated_by` | auditoría | `created_by` = `system` en envíos async |

> Se decidió **loguear todos los intentos** (no solo fallos): confirmar un envío exitoso
> es útil para disputas ("no me llegó") y para habilitar un futuro reenvío. El volumen es
> despreciable y un scheduler de retención (pendiente) purgará los registros antiguos
> (~90 días) usando el índice sobre `created_at`.

---

## Configuración

En `application.properties`:

```properties
# SMTP
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Remitente de la aplicación
cipolflo.mail.from=${MAIL_USERNAME}
cipolflo.mail.nombre-remitente=CIPOLFLO
```

Las credenciales van por variables de entorno (ver `.env.example`). Con Gmail, `MAIL_PASSWORD`
debe ser una **App Password**, no la contraseña de la cuenta.

En tests (`src/test/resources/application.properties`) se define un `spring.mail.host`
dummy para que Spring autoconfigure el `JavaMailSender`; los tests no envían correos.

---

## Cómo agregar un nuevo email

Ejemplo: notificar al confirmar un pago.

1. **Agregar el valor** en `TipoEventoEmail` (ej. `PAGO_REGISTRADO`).
2. **Crear el evento**, ej. `PagoRegistradoEvent(Long pagoId)`.
3. **Publicarlo** en el service correspondiente, dentro del método `@Transactional`, tras
   guardar: `eventPublisher.publishEvent(new PagoRegistradoEvent(pago.getId()))`.
4. **Crear el listener** con `@Async("mailExecutor")` + `@TransactionalEventListener(AFTER_COMMIT)`,
   que resuelve los datos, arma la `SolicitudEmail` y llama a `emailService.enviar(...)`.

El logging es automático: al pasar por `EmailService`, cualquier evento nuevo queda
registrado en `envio_emails_logs` sin código extra.

Para **adjuntar un PDF** (ej. un comprobante), usar
`SolicitudEmail.conAdjuntos(...)` con una lista de `EmailAdjunto`.

---

## Notas de entorno local

En la máquina de desarrollo, el antivirus **Avast** puede interceptar la conexión TLS al
SMTP y hacer fallar el envío con `PKIX path building failed`. Es un problema **solo local**
(no ocurre en producción). Ver detalle y solución en las notas del proyecto.
