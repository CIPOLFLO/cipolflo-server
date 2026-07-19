package com.cipolflo.server.integraciones.telegram.exception;

/**
 * El header {@code X-Telegram-Bot-Api-Secret-Token} falta o no coincide con
 * {@code cipolflo.telegram.webhook-secret}. Es el único caso de este ticket que llega
 * al {@code GlobalExceptionHandler}: viaja por el hilo de request HTTP del webhook.
 */
public class TelegramSecretInvalidoException extends RuntimeException {

    public TelegramSecretInvalidoException(String mensaje) {
        super(mensaje);
    }
}
