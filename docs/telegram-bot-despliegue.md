# Bot de Telegram — Qué hace falta para desplegar

> Checklist de lo que hay que hacer (y lo que **no** hay que tocar) para poner el bot en
> producción. El código no distingue ambiente — todo lo que cambia es configuración y
> pasos operativos puntuales, no lógica.

---

## Índice

1. [Resumen: qué cambia respecto a local](#resumen-qué-cambia-respecto-a-local)
2. [Restricción: una sola instancia](#restricción-una-sola-instancia)
3. [Variables de entorno de producción](#variables-de-entorno-de-producción)
4. [Orden de despliegue](#orden-de-despliegue)
5. [Registrar el webhook](#registrar-el-webhook)
6. [Dar de alta a las personas autorizadas](#dar-de-alta-a-las-personas-autorizadas)
7. [Verificación post-deploy](#verificación-post-deploy)
8. [Si hay que rotar el token del bot](#si-hay-que-rotar-el-token-del-bot)
9. [Qué NO hace falta hacer](#qué-no-hace-falta-hacer)

---

## Resumen: qué cambia respecto a local

Nada en el código. Lo que cambia son tres cosas operativas:

1. Las credenciales (`TELEGRAM_BOT_TOKEN`, `TELEGRAM_WEBHOOK_SECRET`, `AI_API_KEY`) pasan
   a ser las de **producción**, no las de tu bot de pruebas.
2. El webhook se registra contra el **dominio real** (el que ya tenés comprado + el ALB
   de Terraform), no contra un túnel.
3. Los chats autorizados se dan de alta contra el **CRUD de producción**
   (`/api/v1/ajustes/clientes-telegram`), no contra tu Postgres local.

---

## Restricción: una sola instancia

**La memoria conversacional del bot vive en RAM** (`InMemoryChatMemoryRepository`, ver
[`telegram-bot-arquitectura.md`](telegram-bot-arquitectura.md#memoria-conversacional)).
Esto es válido **únicamente si el backend corre en una sola instancia** detrás del ALB.

Si el Terraform escala a más de una instancia (auto scaling group con `desired_count > 1`,
o cualquier configuración equivalente), los mensajes de un mismo chat pueden caer en
instancias distintas, cada una con su propio historial parcial en RAM — las repreguntas
encadenadas ("¿y en abril?") dejarían de funcionar de forma silenciosa e intermitente
(no un error, una respuesta rara).

**Antes de habilitar auto-scaling en el servicio que corre este backend**, hay que migrar
la memoria a `JdbcChatMemoryRepository` (persistida en PostgreSQL) — es un cambio de una
línea en `AsistenteConfig`, pero hay que hacerlo *antes* de escalar, no después. Si el
Terraform actual ya corre una sola instancia fija, no hay nada que hacer todavía; solo
quedar atento si eso cambia.

---

## Variables de entorno de producción

Adicionales a las que ya necesita el resto de la app (DB, Auth0, mail, Azure):

| Variable | De dónde sale | ¿Puede ser la misma que en local? |
|---|---|---|
| `TELEGRAM_BOT_TOKEN` | @BotFather, un bot **nuevo y separado** del de desarrollo | **No** — usar el mismo bot en dos ambientes genera conflictos entre `getUpdates`/`setWebhook` si alguien vuelve a probar en local con webhook, y mezclás tráfico de prueba con el real |
| `TELEGRAM_WEBHOOK_SECRET` | Generado (`openssl rand -hex 32`) | No es obligatorio que sea distinto, pero se recomienda — es gratis generarlo de nuevo y limita el impacto si el de local se filtra |
| `AI_API_KEY` | [console.groq.com](https://console.groq.com), preferentemente una key separada | Recomendado que sea distinta, para no mezclar consumo/rate-limit de pruebas con el real |

Estas se cargan en el Terraform de la misma forma que ya se cargan `DB_PASSWORD`,
`AUTH0_ISSUER_URI`, etc. (secrets manager / variables del task definition — lo que ya
esté usando el proyecto para el resto de las credenciales).

---

## Orden de despliegue

1. **Crear el bot de producción** en BotFather (`/newbot`) — nombre y username definitivos,
   ej. `CipolfloBot` (sin el `_dev`).
2. **Generar el `TELEGRAM_WEBHOOK_SECRET`** de producción.
3. **Cargar las tres variables** en el Terraform / secrets manager del ambiente.
4. **Desplegar** (`terraform apply` o el flujo que corresponda). La migración Liquibase
   `DEV-149` corre sola al arrancar — crea `telegram_chat_autorizado` y
   `envio_mensajes_log` automáticamente, no hace falta ningún paso manual de schema.
5. **Confirmar que el health check responde** en el dominio real:
   ```
   curl https://<tu-dominio>/api/health
   ```
6. Recién ahí, **registrar el webhook** (siguiente sección).

---

## Registrar el webhook

Una sola vez (o cada vez que cambie el dominio o se rote el token):

```
curl -X POST "https://api.telegram.org/bot<TOKEN_PROD>/setWebhook" \
     -d "url=https://<tu-dominio>/api/public/telegram/webhook" \
     -d "secret_token=<TELEGRAM_WEBHOOK_SECRET_PROD>"
```

Verificar:

```
curl "https://api.telegram.org/bot<TOKEN_PROD>/getWebhookInfo"
```

Confirmar `"url"` apuntando al dominio correcto y que no haya `last_error_message`.
Telegram exige HTTPS con certificado válido — si el ALB ya tiene ACM configurado para el
resto de la API, esto ya está resuelto, no hace falta nada extra.

> Si corrés este `curl` desde una máquina Windows con Avast (o antivirus similar) y te da
> un error de certificado, agregá `--ssl-no-revoke` — ver
> [`telegram-bot-pruebas-locales.md`](telegram-bot-pruebas-locales.md#problemas-conocidos).
> Es un problema de esa máquina puntual, no del servidor.

---

## Dar de alta a las personas autorizadas

Ya no es un `INSERT` manual: hay un CRUD (`ClienteTelegramController`, módulo `ajustes`)
protegido con `@PreAuthorize("isAuthenticated()")` en
`/api/v1/ajustes/clientes-telegram` — ver el detalle de cada endpoint en
[`api-contrato.md`](api-contrato.md#get-apiv1ajustesclientes-telegram). Se usa desde la
pantalla de Ajustes del frontend con el mismo JWT de Auth0 que el resto de la app; no
requiere acceso directo a la base de producción.

No requiere acceso a los logs del servidor: el bot le devuelve el `chatId` directo a la
persona en el mensaje de rechazo.

1. Compartile a la persona el link directo al bot de producción:
   `https://t.me/<username_del_bot>`.
2. La persona le escribe cualquier mensaje. Como todavía no está autorizada, el bot le
   responde con su propio `chatId`:
   ```
   No tenés acceso a este bot. Tu ID es 123456789 — pasaselo a un administrador
   para que te dé de alta.
   ```
3. La persona te pasa ese número.
4. Un administrador lo da de alta desde Ajustes (o directo con `POST`, si todavía no está
   la pantalla):
   ```
   POST /api/v1/ajustes/clientes-telegram
   { "chatId": <chatId>, "alias": "<Nombre de la persona>", "recibeNotificaciones": true }
   ```
5. La persona vuelve a escribirle al bot — ahora debería responder normalmente.

Para deshabilitar o dar de baja a alguien (sin recibir notificaciones o sin acceso al
bot), usar `PATCH .../{id}/habilitacion` o `DELETE .../{id}` respectivamente — tampoco
hace falta tocar la base directamente.

> **Por qué el primer contacto sigue sin ser 100% self-service**: Telegram no expone
> ninguna forma de conocer el `chatId` de alguien antes de que le escriba al bot — no hay
> "buscar por username". Por eso el primer contacto siempre requiere que alguien con
> acceso a Ajustes haga el alta una vez que la persona le escribió al bot y obtuvo su
> `chatId`. Una mejora posible (no construida, fuera de alcance): un flujo de auto-alta
> por código de invitación (`/start CODIGO`, el bot se auto-registra si el código
> coincide con uno pre-compartido).

---

## Verificación post-deploy

- [ ] `getWebhookInfo` sin `last_error_message` y con la URL correcta.
- [ ] Un mensaje de prueba desde un chat ya autorizado obtiene respuesta.
- [ ] Un mensaje desde un chat **no** autorizado recibe el rechazo (confirma que la
      autorización funciona en producción, no solo en local).
- [ ] `/reset` funciona.
- [ ] Revisar `envio_mensajes_log` en la base: debería haber filas `ENVIADO` para las
      pruebas de arriba.

---

## Si hay que rotar el token del bot

Si el `TELEGRAM_BOT_TOKEN` de producción se filtra:

1. `/revoke` en BotFather → genera un token nuevo para el mismo bot.
2. Actualizar `TELEGRAM_BOT_TOKEN` en el secrets manager / Terraform y redesplegar (o
   reiniciar el servicio, según cómo esté armado el pipeline).
3. Volver a correr `setWebhook` con el token nuevo (el registro del webhook queda atado
   al token viejo).

---

## Qué NO hace falta hacer

- **No** hay que tocar el código para cambiar de ambiente — todo es configuración.
- **No** hay que crear la tabla `telegram_chat_autorizado` a mano — la crea Liquibase.
- **No** hace falta ngrok, `serveo` ni ningún túnel — el ALB ya es público.
- **No** hay que exponer ningún endpoint nuevo en `SecurityConfig` — `/api/public/**` ya
  está permitido.
