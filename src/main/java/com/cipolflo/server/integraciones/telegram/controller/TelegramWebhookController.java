package com.cipolflo.server.integraciones.telegram.controller;

import com.cipolflo.server.integraciones.telegram.client.TelegramProperties;
import com.cipolflo.server.integraciones.telegram.dto.TelegramUpdateDto;
import com.cipolflo.server.integraciones.telegram.exception.TelegramSecretInvalidoException;
import com.cipolflo.server.integraciones.telegram.processor.IProcesadorMensajeTelegram;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Recibe los updates de Telegram. Es el único endpoint del sistema sin {@code @PreAuthorize}:
 * Telegram no puede mandar un JWT de Auth0, así que la autenticación la hace el header
 * {@code X-Telegram-Bot-Api-Secret-Token} comparado contra el secret configurado. El path va
 * bajo {@code /api/public/**}, ya permitido en {@code SecurityConfig}.
 *
 * La respuesta es siempre {@code 200} una vez validado el secret, aun cuando el update se
 * descarte o el procesamiento falle — un status distinto de 2xx hace que Telegram reintente
 * el mismo update indefinidamente. El procesamiento real ocurre en un hilo aparte
 * ({@link IProcesadorMensajeTelegram} es {@code @Async}), así que el 200 no espera a la
 * latencia del modelo de IA.
 */
@RestController
@RequestMapping("/api/public/telegram")
public class TelegramWebhookController {

    private static final String HEADER_SECRET = "X-Telegram-Bot-Api-Secret-Token";

    private final TelegramProperties telegramProperties;
    private final IProcesadorMensajeTelegram procesadorMensaje;

    public TelegramWebhookController(TelegramProperties telegramProperties,
                                     IProcesadorMensajeTelegram procesadorMensaje) {
        this.telegramProperties = telegramProperties;
        this.procesadorMensaje = procesadorMensaje;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirUpdate(
            @RequestBody TelegramUpdateDto update,
            @RequestHeader(value = HEADER_SECRET, required = false) String secret) {

        if (!secretValido(secret)) {
            throw new TelegramSecretInvalidoException("Secret de webhook inválido o ausente");
        }

        procesadorMensaje.procesar(update);
        return ResponseEntity.ok().build();
    }

    /**
     * Compara en tiempo constante ({@link MessageDigest#isEqual}) para no filtrar el secret
     * por diferencias de tiempo de respuesta según cuántos caracteres coincidan de más
     * (a diferencia de {@code String.equals}, que corta apenas encuentra la primera diferencia).
     */
    private boolean secretValido(String secret) {
        if (secret == null) {
            return false;
        }
        byte[] recibido = secret.getBytes(StandardCharsets.UTF_8);
        byte[] esperado = telegramProperties.webhookSecret().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(recibido, esperado);
    }
}
