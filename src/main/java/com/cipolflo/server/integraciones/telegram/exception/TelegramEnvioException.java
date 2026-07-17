package com.cipolflo.server.integraciones.telegram.exception;

/**
 * El envío por la API de Telegram falló después de agotar los reintentos. La lanza
 * {@code TelegramApiClient}; la consumen sus llamadores (el procesador de mensajes y
 * las notificaciones), que la loguean y contienen. Nunca viaja por un hilo de request
 * HTTP, así que no lleva handler en {@code GlobalExceptionHandler}.
 */
public class TelegramEnvioException extends RuntimeException {

    public TelegramEnvioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
