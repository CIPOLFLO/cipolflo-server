package com.cipolflo.server.shared.email.exception;

public class DestinatarioNotificacionEmailNoEncontradoException extends RuntimeException {

    public DestinatarioNotificacionEmailNoEncontradoException(Long id) {
        super("Destinatario de notificaciones por email no encontrado con id: " + id);
    }
}
