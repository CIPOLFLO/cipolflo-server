# Bot de Telegram — Arquitectura (DEV-149)

> Qué se construyó, cómo están organizadas las piezas y cómo fluye un mensaje de punta a
> punta. Para la configuración inicial (dependencias, credenciales, Groq) ver
> [`telegram-groq-setup.md`](telegram-groq-setup.md). Para desplegar, ver
> [`telegram-bot-despliegue.md`](telegram-bot-despliegue.md).

---

## Índice

1. [Resumen](#resumen)
2. [Estructura de paquetes](#estructura-de-paquetes)
3. [Flujo de una consulta entrante](#flujo-de-una-consulta-entrante)
4. [Piezas, una por una](#piezas-una-por-una)
5. [Base de datos](#base-de-datos)
6. [Memoria conversacional](#memoria-conversacional)
7. [Manejo de errores](#manejo-de-errores)
8. [Tests](#tests)
9. [Qué NO está implementado](#qué-no-está-implementado)

---

## Resumen

El bot resuelve **consultas entrantes en lenguaje natural** sobre disponibilidad de
servicios y estado de cuota de socios. El usuario le escribe a `Cipolflo_dev_bot` en
Telegram, Spring AI (contra Groq) interpreta la intención y decide si invocar una de dos
`@Tool`: `consultarDisponibilidad` o `consultarEstadoCuota`. El modelo **nunca** toca la
base de datos ni el esquema: solo elige qué función invocar y con qué argumentos.

Esta primera etapa cubre **solo el camino de entrada** (consultas). El envío de
notificaciones salientes (aviso mensual de cuotas atrasadas, Telegram en el reporte
semanal) se evaluó y se decidió **posponerlo** — ver
["Qué NO está implementado"](#qué-no-está-implementado).

---

## Estructura de paquetes

```
integraciones/
├── mensajeria/                    ← núcleo, agnóstico de Telegram
│   ├── ai/
│   │   ├── AsistenteConfig.java           bean ChatClient, system prompt, memoria
│   │   ├── AsistenteConsultas.java        responder(conversacionId, texto)
│   │   ├── AsistenteException.java        envuelve fallas del proveedor de IA
│   │   ├── SesionesConversacion.java      TTL de inactividad (30 min)
│   │   ├── MemoriaConversacionalProperties.java
│   │   └── tools/
│   │       ├── DisponibilidadTools.java   @Tool consultarDisponibilidad
│   │       └── CuotaTools.java            @Tool consultarEstadoCuota
│   ├── puerto/
│   │   ├── CanalMensajeria.java           enviar(destinatarioId, texto, tipoEvento)
│   │   ├── RegistroDestinatarios.java     buscarAutorizado(id), destinatariosDeNotificaciones()
│   │   └── DestinatarioMensajeria.java    record (destinatarioId, alias)
│   └── log/
│       ├── EnvioMensajeLog.java           entidad de envio_mensajes_log
│       ├── EnvioMensajeLogRegistrar.java  persiste en transacción REQUIRES_NEW
│       ├── EnvioMensajeLogRepository.java
│       ├── CanalMensaje.java              enum: TELEGRAM
│       ├── EstadoEnvioMensaje.java        enum: ENVIADO, FALLIDO
│       └── TipoEventoMensaje.java         enum: RESPUESTA_CONSULTA, RECHAZO_NO_AUTORIZADO
└── telegram/                      ← adaptador, conoce el formato de Telegram
    ├── client/
    │   ├── TelegramApiClient.java         implements CanalMensajeria, reintentos
    │   └── TelegramProperties.java        bot-token, webhook-secret
    ├── controller/
    │   └── TelegramWebhookController.java POST /api/public/telegram/webhook
    ├── processor/
    │   ├── IProcesadorMensajeTelegram.java
    │   └── ProcesadorMensajeTelegram.java @Async, orquesta todo el flujo
    ├── domain/
    │   └── TelegramChatAutorizado.java    entidad de telegram_chat_autorizado
    ├── repository/
    │   └── TelegramChatAutorizadoRepository.java  implements RegistroDestinatarios
    ├── dto/
    │   ├── TelegramUpdateDto.java
    │   ├── TelegramMessageDto.java
    │   └── TelegramChatDto.java
    └── exception/
        ├── TelegramCodigoError.java
        ├── TelegramSecretInvalidoException.java
        └── TelegramEnvioException.java
```

La separación **núcleo/adaptador** es deliberada: `mensajeria/` no importa nada de
`telegram/`, ni conoce el formato del update ni el concepto de "chat de Telegram". Solo
conoce sus propios puertos (`CanalMensajeria`, `RegistroDestinatarios`) y un
`destinatarioId` genérico (`String`). Si en el futuro se agrega WhatsApp u otro canal, se
escribe un adaptador nuevo (`integraciones/whatsapp/`) implementando esos mismos puertos —
`mensajeria/` no cambia una línea.

También se agregó, fuera de `integraciones/`, soporte de datos en los módulos dueños:

- `servicios/dto/ServicioReferenciaDto.java` + `IConsultaServicioSimple.buscarPorNombre(...)`
- `clientes/dto/EstadoSocioResponseDto` (se le sumó `mesesSinPagar`)
- `clientes/repository/ClienteRepository.findByEstadoInAndMesesSinPagarGreaterThanEqual(...)`
  y `clientes/service/IConsultaSociosAtrasados` (soporte para un futuro aviso de cuotas
  atrasadas — ver ["Qué NO está implementado"](#qué-no-está-implementado))

---

## Flujo de una consulta entrante

```
Usuario escribe en Telegram
        │
        ▼
POST /api/public/telegram/webhook   (TelegramWebhookController)
        │  valida X-Telegram-Bot-Api-Secret-Token (MessageDigest.isEqual, tiempo constante)
        │  si no coincide → 401 TELEGRAM_SECRET_INVALIDO
        │  si coincide → 200 OK inmediato + delega en el procesador
        ▼
ProcesadorMensajeTelegram.procesar(update)   [@Async("telegramExecutor")]
        │  1. sin texto → debug log, sale
        │  2. RegistroDestinatarios.buscarAutorizado(chatId)
        │       no autorizado → warn log + mensaje de rechazo, sale
        │  3. texto == "/reset" → AsistenteConsultas.reiniciarConversacion(chatId) + confirmación, sale
        │  4. AsistenteConsultas.responder(chatId, texto)
        ▼
AsistenteConsultas (ChatClient de Spring AI)
        │  registra actividad en SesionesConversacion (limpia memoria si venció el TTL)
        │  arma el prompt con la fecha de hoy (Uruguay) vía placeholder {fecha}
        │  el modelo decide si invoca DisponibilidadTools o CuotaTools (o ninguna)
        ▼
CanalMensajeria.enviar(chatId, respuesta, tipoEvento)   (TelegramApiClient)
        │  POST a la API de Telegram, 2 reintentos si 5xx/red
        │  registra ENVIADO/FALLIDO en envio_mensajes_log
        ▼
Usuario recibe la respuesta en Telegram
```

Ningún error en este camino rompe el hilo async ni se propaga hacia Telegram: el `200`
del webhook ya salió antes de que nada de esto ocurra.

---

## Piezas, una por una

### `TelegramWebhookController`

Único endpoint del sistema sin `@PreAuthorize` — Telegram no puede mandar un JWT de
Auth0. Va bajo `/api/public/**` (ya `permitAll()` en `SecurityConfig`). Responde `200`
siempre que el secret sea válido, sin esperar al procesamiento.

### `ProcesadorMensajeTelegram`

Vive en el adaptador porque conoce el formato de `TelegramUpdateDto`. Orquesta todo:
autorización, `/reset`, delegación al asistente, envío de la respuesta, y contiene
cualquier error para que nunca se propague.

### `AsistenteConsultas` / `AsistenteConfig`

`AsistenteConfig` arma el bean `ChatClient`: el system prompt (rol, idioma castellano
rioplatense, fecha de hoy vía placeholder, dos reglas anti-alucinación — no inventar
datos, transcribir todas las coincidencias de una tool), las dos tools registradas, y el
advisor de memoria (`MessageChatMemoryAdvisor`).

`AsistenteConsultas.responder(conversacionId, texto)` es la fachada: registra actividad
en `SesionesConversacion`, arma la fecha de hoy (con `Clock` inyectado, zona Uruguay), y
envuelve cualquier falla del proveedor en `AsistenteException`.

### `DisponibilidadTools` / `CuotaTools`

Ninguna de las dos tools desambigua homónimos: si hay más de una coincidencia, responden
por **todas**, rotuladas (procedencia para servicios, cédula/RUT para clientes), cortando
en 10 con aviso de que hay más. Ninguna lanza excepción ante entrada inválida — devuelven
texto explicativo para que el modelo pueda repreguntar. `CuotaTools` distingue cédula de
RUT por longitud de dígitos normalizados (7-8 = cédula, 12 = RUT).

### `TelegramApiClient`

Implementa `CanalMensajeria` con `RestClient`. Reintenta hasta 2 veces con backoff corto
ante 5xx/errores de red (no ante 4xx). Registra cada intento en `envio_mensajes_log`.

---

## Base de datos

Migración `DEV-149` (`db/changelog/migrations/DEV-149/`):

- **`telegram_chat_autorizado`**: `chat_id` (único), `alias`, `activo`,
  `recibe_notificaciones`. El alta es manual (script/INSERT directo) — no hay endpoint de
  administración. El `chatId` se obtiene escribiéndole al bot y leyendo el log o
  `getUpdates` (ver [setup](telegram-groq-setup.md)).
- **`envio_mensajes_log`**: espejo de `envio_emails_logs`. Una fila por cada intento de
  envío (`ENVIADO`/`FALLIDO`), con `tipo_evento` para distinguir el origen.

---

## Memoria conversacional

- **Ventana de 20 mensajes** (`MessageWindowChatMemory` + `InMemoryChatMemoryRepository`,
  configurable vía `cipolflo.mensajeria.memoria.max-mensajes`).
- **TTL de inactividad de 30 minutos** (`SesionesConversacion`,
  `cipolflo.mensajeria.memoria.ttl-inactividad`): si pasó el TTL, la próxima interacción
  limpia el historial de ese chat antes de continuar.
- **`/reset`**: comando que limpia la conversación, interceptado por el procesador antes
  de llamar al modelo.
- La clave es el `chatId` — cada chat tiene su propia memoria, aislada.

**Restricción crítica**: la memoria vive en RAM (`InMemoryChatMemoryRepository`). Es
válida **solo con una única instancia** del backend corriendo. Ver
[el doc de despliegue](telegram-bot-despliegue.md#restricción-una-sola-instancia) antes
de escalar horizontalmente.

---

## Manejo de errores

Dos fallas, con remedios distintos (documentado también en `ProcesadorMensajeTelegram`):

| Falla | Qué pasa |
|---|---|
| Modelo de IA o una tool | Se le avisa al usuario: *"No pude procesar tu consulta en este momento..."*. El detalle técnico queda en el log, no en el mensaje. |
| Envío por Telegram | No hay forma de avisarle al usuario (es el único canal). Se reintenta 2 veces, se loguea en `error`, y queda registrado como `FALLIDO` en `envio_mensajes_log`. |

Ninguna excepción llega nunca al hilo HTTP del webhook (que ya respondió `200`) ni tumba
el hilo async.

---

## Tests

Todo el pipeline está cubierto con tests unitarios/`@WebMvcTest`, sin pegarle nunca al
proveedor de IA ni a la API de Telegram real:

- `TelegramApiClientTest` — `MockRestServiceServer`, reintentos, registro de log.
- `DisponibilidadToolsTest` / `CuotaToolsTest` — casos de homónimos, cortes de 10, errores
  de validación, cliente deshabilitado, etc.
- `SesionesConversacionTest` — TTL con `Clock` fijo, sin `Thread.sleep`.
- `AsistenteConsultasTest` — `ChatClient` mockeado, verifica `conversacionId` y manejo de
  fallas.
- `ProcesadorMensajeTelegramTest` — autorización, `/reset`, fallas de asistente/canal.
- `TelegramWebhookControllerTest` — secret ausente/incorrecto/correcto, update sin texto.

---

## Qué NO está implementado

Deliberadamente fuera de esta etapa (se evaluó y se decidió posponer):

- **Notificaciones salientes por Telegram**: el aviso mensual de socios con cuotas
  atrasadas, y sumar Telegram al reporte semanal de reservas (hoy solo va por mail). Se
  llegó a diseñar un puerto genérico de difusión (`IDifusorNotificacion`) pero se
  descartó por prematuro — con un solo caso de uso real, agregaba indirección sin
  necesidad. Si se retoma, conviene revisar primero si conviene una implementación
  directa (mail + Telegram inline en el service) antes de reintroducir una abstracción.
- Lo que sí quedó listo y reutilizable para cuando se retome: `IConsultaSociosAtrasados`
  (clientes con cuotas atrasadas, ACTIVO/INACTIVO, ≥1 mes impago) y
  `Socio.MESES_PARA_INACTIVO` como constante nombrada.
- **Reservas por vencer**: mencionado en el ticket original, no se tocó nada (ni la
  query de `ReservaRepository` ni el scheduler).
- **Registro real del webhook en producción**: se probó exitosamente en local (con un
  túnel SSH vía `serveo.net`, porque `ngrok` no funcionaba localmente por
  interceptación TLS de Avast). El registro contra el dominio real queda pendiente del
  despliegue — ver [`telegram-bot-despliegue.md`](telegram-bot-despliegue.md).
