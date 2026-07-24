package com.cipolflo.server.integraciones.telegram.exception;

public class TelegramChatNoEncontradoException extends RuntimeException {

    public TelegramChatNoEncontradoException(Long id) {
        super("Cliente autorizado de Telegram no encontrado con id: " + id);
    }
}
