package com.cipolflo.server.shared.email;

/**
 * Error al construir o enviar un email. Envuelve las excepciones de bajo nivel
 * (jakarta.mail / Spring Mail) para exponer una API uniforme.
 */
public class EmailException extends RuntimeException {
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
