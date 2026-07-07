package com.cipolflo.server.shared.email;

/**
 * Evento de negocio que originó el envío de un email. Se persiste en la tabla
 * envio_emails_logs para poder rastrear/filtrar los correos por su origen.
 */
public enum TipoEventoEmail {
    RESERVA_CREADA,
    REPORTE_SEMANAL_RESERVAS
}
