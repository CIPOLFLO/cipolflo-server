package com.cipolflo.server.shared.email;

/**
 * Envío de correos de la aplicación. Cada envío se registra en la tabla
 * envio_emails_logs (ver {@link EnvioEmailLogRegistrar}).
 *
 * Implementación sincrónica: el llamador decide si lo invoca de forma asíncrona
 * (ver listeners con {@code @Async}).
 */
public interface IEmailService {

    /**
     * Construye y envía el email descrito por {@code solicitud}, y deja registrado
     * el resultado (ENVIADO / FALLIDO). Lanza {@link EmailException} si el envío falla.
     */
    void enviar(SolicitudEmail solicitud);
}
