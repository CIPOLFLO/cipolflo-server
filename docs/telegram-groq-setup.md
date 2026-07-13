# Setup de Telegram + Groq (DEV-149) — CIPOLFLO Server

> Qué se configuró para el bot de Telegram (RF6) y cómo se obtuvo cada credencial.
> Este documento cubre el **paso 1** del ticket (dependencias, properties y alta del
> bot). La arquitectura completa ya implementada (tools, asistente, webhook) está en
> [`telegram-bot-arquitectura.md`](telegram-bot-arquitectura.md); para probarlo en local
> ver [`telegram-bot-pruebas-locales.md`](telegram-bot-pruebas-locales.md); para
> desplegar, [`telegram-bot-despliegue.md`](telegram-bot-despliegue.md).

---

## Índice

1. [Resumen](#resumen)
2. [Dependencias (`build.gradle.kts`)](#dependencias-buildgradlekts)
3. [Properties (`application.properties`)](#properties-applicationproperties)
4. [Variables de entorno (`.env.example`)](#variables-de-entorno-envexample)
5. [De dónde sale cada credencial](#de-dónde-sale-cada-credencial)
6. [Probar el bot en local sin webhook](#probar-el-bot-en-local-sin-webhook)
7. [Problema conocido: curl + Avast (Windows)](#problema-conocido-curl--avast-windows)
8. [Qué falta](#qué-falta)

---

## Resumen

El bot usa **Spring AI** contra **Groq** (proveedor con API compatible con OpenAI) para
interpretar consultas en lenguaje natural, y **Telegram** como canal de mensajería. Este
documento cubre específicamente el paso 1 (dependencias, properties, credenciales) —
el resto del bot (tabla de chats autorizados, webhook, tools, asistente) ya está
implementado; ver [`telegram-bot-arquitectura.md`](telegram-bot-arquitectura.md).

---

## Dependencias (`build.gradle.kts`)

```kotlin
// Spring AI 2.0.0 exige Spring Boot 4.x. Mientras el proyecto esté en Boot 3.5.x
// se fija la última versión estable de la línea 1.x.
val springAiVersion = "1.1.8"

dependencies {
    // ...
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}
```

`spring-ai-starter-model-openai` es el starter genérico de OpenAI: sirve para Groq porque
Groq expone un endpoint compatible, cambiando solo `base-url`. Se fijó la versión `1.1.8`
(última de la línea 1.x) porque la `2.0.0` del BOM requiere Spring Boot 4.x y el proyecto
está en `3.5.14`.

---

## Properties (`application.properties`)

```properties
# Telegram
# Las credenciales se leen desde variables de entorno - ver .env.example
cipolflo.telegram.bot-token=${TELEGRAM_BOT_TOKEN:}
cipolflo.telegram.webhook-secret=${TELEGRAM_WEBHOOK_SECRET:}

# Spring AI - proveedor Groq via API compatible con OpenAI
# El starter de OpenAI autoconfigura ademas modelos de audio, imagen, moderacion
# y embeddings, todos exigiendo la api-key al arrancar aunque no se usen. Se
# deshabilitan aca porque el bot solo necesita chat (con tool calling).
spring.ai.model.chat=openai
spring.ai.model.audio.speech=none
spring.ai.model.audio.transcription=none
spring.ai.model.image=none
spring.ai.model.moderation=none
spring.ai.model.embedding=none

spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.api-key=${AI_API_KEY:}
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
spring.ai.openai.chat.options.temperature=0.2

# Memoria conversacional del asistente (por chat)
cipolflo.mensajeria.memoria.max-mensajes=20
cipolflo.mensajeria.memoria.ttl-inactividad=30m
```

Las properties de los **crons nuevos** (aviso de cuotas atrasadas, aviso de reservas por
vencer) se dejaron **fuera a propósito** — se agregan recién cuando se construyan esos
schedulers, no en esta etapa de configuración.

### Por qué se deshabilitan audio/imagen/moderación/embeddings

`spring-ai-starter-model-openai` no trae solo el modelo de chat: autoconfigura también
`OpenAiAudioSpeechModel`, `OpenAiAudioTranscriptionModel`, `OpenAiImageModel`,
`OpenAiModerationModel` y `OpenAiEmbeddingModel`. Cada uno valida su propia API key **al
arrancar el contexto**, aunque nunca se use — sin este apagado explícito, la app no
levanta (`BeanCreationException: OpenAI API key must be set` sobre
`openAiAudioSpeechModel`, por ejemplo). Se desactivan con las properties
`spring.ai.model.*=none`, dejando habilitado solo `spring.ai.model.chat=openai`.

### Tests

`src/test/resources/application.properties` reemplaza por completo (no extiende) al
`application.properties` de main durante los tests, así que necesita su propia
configuración mínima de Spring AI para que `ServerApplicationTests` (el único test que
levanta el contexto completo) no falle:

```properties
spring.ai.model.chat=openai
spring.ai.model.audio.speech=none
spring.ai.model.audio.transcription=none
spring.ai.model.image=none
spring.ai.model.moderation=none
spring.ai.model.embedding=none
spring.ai.openai.api-key=test-key
```

La key es dummy: los tests no le pegan al proveedor real, pero Spring AI la exige no
vacía para construir el bean.

---

## Variables de entorno (`.env.example`)

```
# ── Telegram ──────────────────────────────────────────────────────────────────
TELEGRAM_BOT_TOKEN=tu_token_de_botfather_aqui
TELEGRAM_WEBHOOK_SECRET=tu_secret_generado_aqui

# ── IA (Groq, API compatible con OpenAI) ───────────────────────────────────────
AI_API_KEY=tu_api_key_de_groq_aqui
```

---

## De dónde sale cada credencial

| Variable | Origen | Notas |
|---|---|---|
| `TELEGRAM_BOT_TOKEN` | Telegram, hablando con **@BotFather** → `/newbot` | Da control total del bot. Si se filtra, se revoca con `/revoke` en BotFather y se genera uno nuevo. |
| `TELEGRAM_WEBHOOK_SECRET` | **No lo da Telegram — lo generamos nosotros** | String arbitrario (`openssl rand -hex 32`). Se lo declaramos a Telegram al registrar el webhook (`setWebhook`, todavía no hecho); a partir de ahí Telegram nos lo devuelve en el header `X-Telegram-Bot-Api-Secret-Token` en cada request, y así confirmamos que la llamada es realmente de Telegram. |
| `AI_API_KEY` | [console.groq.com](https://console.groq.com) → **API Keys** → *Create API Key* | Se muestra una sola vez al crearla (`gsk_...`). Tiene tier gratuito, alcanza para este uso (2-3 usuarios internos). |

Bot creado para desarrollo: **`Cipolflo_dev_bot`**.

---

## Probar el bot en local sin webhook

Telegram exige HTTPS público para el webhook — no acepta `localhost`, así que en esta
etapa (sin `TelegramWebhookController` todavía) se usa **`getUpdates`** para confirmar
que el token funciona y para obtener el `chatId` de cada persona autorizada:

1. Escribirle cualquier mensaje de texto al bot desde Telegram (ej. `hola`).
2. Correr:
   ```
   curl "https://api.telegram.org/bot<TOKEN>/getUpdates"
   ```
3. La respuesta trae el mensaje con `message.chat.id` — ese es el `chatId` que después
   se inserta en `telegram_chat_autorizado` (migración pendiente, paso 3 del ticket).

`getUpdates` y el webhook (`setWebhook`) son **excluyentes**: si en algún momento se
registra un webhook para este bot, `getUpdates` deja de devolver resultados hasta que se
borre el webhook. Por eso conviene usar un bot de desarrollo separado del de producción.

---

## Problema conocido: curl + Avast (Windows)

En la máquina de desarrollo, `curl` (backend `schannel` de Windows) puede fallar contra
`api.telegram.org` con:

```
curl: (35) schannel: next InitializeSecurityContext failed: CRYPT_E_NO_REVOCATION_CHECK
```

Es el mismo problema de fondo que afecta al envío de mails por SMTP en local: **Avast**
intercepta el tráfico HTTPS (HTTPS scanning / Web Shield) y eso rompe la verificación de
revocación del certificado. Workaround, saltear solo esa verificación puntual:

```
curl --ssl-no-revoke "https://api.telegram.org/bot<TOKEN>/getUpdates"
```

No es necesario para la aplicación en sí (el `RestClient` de Spring usa el store de
certificados de Java, no `schannel`) — es un problema solo de `curl` en la terminal de
Windows.

---

## Qué falta

Esto cubre únicamente el paso 1 (configuración inicial). El resto del camino de
**consultas entrantes** ya está implementado y documentado en
[`telegram-bot-arquitectura.md`](telegram-bot-arquitectura.md): migración, puertos,
cliente de Telegram, procesador, webhook, tools y asistente de IA.

Lo que sigue pendiente (ver la sección
["Qué NO está implementado"](telegram-bot-arquitectura.md#qué-no-está-implementado) de
ese documento):

- Notificaciones salientes por Telegram (aviso de cuotas atrasadas, reporte semanal
  bicanal) — se evaluó y se decidió posponer.
- El scheduler de "reservas por vencer" mencionado en el ticket original.
- Registrar el webhook en el ambiente desplegado — ver
  [`telegram-bot-despliegue.md`](telegram-bot-despliegue.md).

El ticket original completo está en
[`docs/tickets/DEV-149-bot-telegram.md`](tickets/DEV-149-bot-telegram.md).
