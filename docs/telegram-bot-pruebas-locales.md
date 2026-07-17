# Bot de Telegram — Cómo probarlo en local (sin desplegar)

> Guía para que cualquiera del equipo pueda levantar el bot en su máquina y probarlo de
> punta a punta (mandarle un mensaje real y que responda), sin necesidad de un despliegue.
> Para entender qué hace cada pieza, ver
> [`telegram-bot-arquitectura.md`](telegram-bot-arquitectura.md). Para desplegar de
> verdad, ver [`telegram-bot-despliegue.md`](telegram-bot-despliegue.md).

---

## Índice

1. [Idea general](#idea-general)
2. [Paso 1 — Credenciales propias](#paso-1--credenciales-propias)
3. [Paso 2 — Levantar la base y el backend](#paso-2--levantar-la-base-y-el-backend)
4. [Paso 3 — Abrir un túnel público](#paso-3--abrir-un-túnel-público)
5. [Paso 4 — Registrar el webhook](#paso-4--registrar-el-webhook)
6. [Paso 5 — Autorizar tu chat](#paso-5--autorizar-tu-chat)
7. [Paso 6 — Probarlo](#paso-6--probarlo)
8. [Cortar la prueba](#cortar-la-prueba)
9. [Problemas conocidos](#problemas-conocidos)

---

## Idea general

Telegram le manda los mensajes a tu backend vía **webhook**: un `POST` a una URL pública
HTTPS que vos le indicás. Como tu backend corre en `localhost:8080`, hace falta un
**túnel** que le dé una URL pública temporal a tu máquina. El flujo completo es:

```
Telegram → URL pública del túnel → tu backend en localhost:8080
```

**Usá tu propio bot para esto, no el bot compartido del equipo.** `getUpdates` (polling)
y `setWebhook` (webhook) son excluyentes en un mismo bot — si vos registrás un webhook
apuntando a tu túnel, le rompés las pruebas a cualquier otro compañero que esté usando
ese mismo bot en simultáneo. Crear un bot nuevo con BotFather toma un minuto y es gratis.

---

## Paso 1 — Credenciales propias

1. **Bot de Telegram**: hablale a **@BotFather** → `/newbot` → elegí un nombre y un
   username que termine en `bot` (ej. `TuNombreCipolfloDevBot`). Te da un `TELEGRAM_BOT_TOKEN`.
2. **Webhook secret**: lo inventás vos, no lo da Telegram. Cualquier string random sirve:
   ```
   openssl rand -hex 32
   ```
3. **API key de Groq**: [console.groq.com](https://console.groq.com) → *API Keys* →
   *Create API Key* (gratis, no pide tarjeta).

Copiá `.env.example` a `.env` (si no lo tenés ya) y completá las tres:

```
TELEGRAM_BOT_TOKEN=<tu token de BotFather>
TELEGRAM_WEBHOOK_SECRET=<el string que generaste>
AI_API_KEY=<tu key de Groq>
```

> **Ojo con espacios ocultos.** Si copiás el token desde el chat de Telegram y queda un
> espacio de más al pegarlo en `.env`, la app arma mal la URL contra la API de Telegram y
> todo falla en silencio o con errores raros (`HTTP 404`, `URL rejected`). Si algo no
> funciona y no ves por qué, primero mirá el valor del token con lupa.

---

## Paso 2 — Levantar la base y el backend

```
docker-compose up -d          # levanta Postgres
./gradlew bootRun             # o desde tu IDE
```

Confirmá que responde:

```
curl http://localhost:8080/api/health
```

---

## Paso 3 — Abrir un túnel público

**Opción A — ngrok** (la más simple, probá esta primero):

```
ngrok http 8080
```

Te va a imprimir una URL tipo `https://algo-random.ngrok-free.app`. Copiala.

**Opción B — túnel SSH (`serveo.net`)**, si ngrok no te conecta:

```
ssh -R 80:localhost:8080 serveo.net
```

Te imprime una URL tipo `https://algo-random.serveousercontent.com`.

> **Por qué existe la opción B**: en máquinas con un antivirus que inspecciona tráfico
> HTTPS (Avast, y probablemente otros similares), `ngrok` puede fallar con
> `certificate signed by unknown authority` — `ngrok` fija (pinea) sus propios
> certificados y no respeta las excepciones que le agregues al antivirus salvo que
> excluyas el ejecutable de `ngrok.exe` específicamente ahí. Un túnel SSH no pasa por la
> inspección HTTPS del antivirus (es otro protocolo, otro puerto), así que lo esquiva por
> completo. Si usás Avast: `Configuración → General → Excepciones → agregar ngrok.exe`.

Dejá esta terminal abierta — el túnel muere si la cerrás.

---

## Paso 4 — Registrar el webhook

Con la URL del túnel del paso 3:

```
curl --ssl-no-revoke -X POST "https://api.telegram.org/bot<TU_TOKEN>/setWebhook" \
     -d "url=<URL_DEL_TUNEL>/api/public/telegram/webhook" \
     -d "secret_token=<TU_TELEGRAM_WEBHOOK_SECRET>"
```

Debería responder `{"ok":true,"result":true,"description":"Webhook was set"}`.

Verificar en cualquier momento:

```
curl --ssl-no-revoke "https://api.telegram.org/bot<TU_TOKEN>/getWebhookInfo"
```

(`--ssl-no-revoke` es el mismo workaround del problema de Avast con `curl` — ver
[problemas conocidos](#problemas-conocidos). Si tu máquina no tiene ese problema, no hace falta.)

---

## Paso 5 — Autorizar tu chat

El bot rechaza cualquier chat que no esté en `telegram_chat_autorizado`. Para darte de alta:

1. Escribile cualquier mensaje a tu bot desde Telegram (ej. `hola`).
2. Como todavía no estás autorizado, el bot te responde con tu propio `chatId` directo en
   el chat (no hace falta mirar logs del servidor):
   ```
   No tenés acceso a este bot. Tu ID es 123456789 — pasaselo a un administrador
   para que te dé de alta.
   ```
3. Insertalo en tu base local:
   ```sql
   INSERT INTO telegram_chat_autorizado (chat_id, alias, activo, recibe_notificaciones, created_at, updated_at, created_by, updated_by)
   VALUES (123456789, 'Tu Nombre', true, true, now(), now(), 'system', 'system');
   ```

---

## Paso 6 — Probarlo

Escribile al bot cosas como:

- `hola` → debería responder con un resumen de qué puede consultar.
- `¿está libre la cabaña 2 del 10 al 15 de marzo?` → `consultarDisponibilidad`.
- `¿el socio de cédula 12345678 está al día?` → `consultarEstadoCuota`.
- `/reset` → limpia la conversación, sin gastar tokens del modelo.

---

## Cortar la prueba

Cuando termines, borrá el webhook para que Telegram deje de intentar entregarte mensajes
contra un túnel que ya cerraste:

```
curl --ssl-no-revoke "https://api.telegram.org/bot<TU_TOKEN>/deleteWebhook"
```

Y cerrá la terminal del túnel (`Ctrl+C`).

---

## Problemas conocidos

| Síntoma | Causa | Solución |
|---|---|---|
| `curl: (35) schannel: ... CRYPT_E_NO_REVOCATION_CHECK` | Avast intercepta HTTPS y rompe la verificación de revocación del certificado | Agregar `--ssl-no-revoke` a `curl` |
| `ngrok`: `certificate signed by unknown authority` | Avast intercepta HTTPS y `ngrok` pinea sus propios certificados (no respeta el store de Windows) | Agregar excepción para `ngrok.exe` en Avast, o usar el túnel SSH (opción B del paso 3) |
| El bot no responde nada y no hay error visible | Puede ser un espacio de más en `TELEGRAM_BOT_TOKEN` en `.env` (ver nota en el paso 1) | Revisar el valor exacto del token |
| `getUpdates` devuelve `result: []` siempre | Hay un webhook registrado para ese bot — son excluyentes | `deleteWebhook`, o usar un bot distinto para cada modo |
| El bot dice que no encuentra algo que sabés que existe | Puede ser el modelo de IA no invocando la tool correctamente (pasa a veces con modelos gratuitos) | Revisar el log del backend: si las queries de Hibernate para esa consulta aparecen, la tool sí se ejecutó — el problema es de redacción del modelo, no del código. Reintentar suele alcanzar. |
