package com.cipolflo.server.shared.email;

import java.util.List;

/**
 * Todo lo necesario para enviar un email y dejarlo registrado en el log.
 * Incluye el contexto de negocio ({@code tipoEvento}, {@code referenciaId}) para
 * que {@link EmailService} pueda persistir el registro de forma centralizada.
 *
 * @param referenciaId id de la entidad que originó el mail (ej. la reserva), puede ser null
 */
public record SolicitudEmail(
        String destinatario,
        String asunto,
        String cuerpo,
        boolean html,
        List<EmailAdjunto> adjuntos,
        TipoEventoEmail tipoEvento,
        Long referenciaId
) {
    public static SolicitudEmail texto(String destinatario, String asunto, String cuerpo,
                                       TipoEventoEmail tipoEvento, Long referenciaId) {
        return new SolicitudEmail(destinatario, asunto, cuerpo, false, List.of(), tipoEvento, referenciaId);
    }

    public static SolicitudEmail conAdjuntos(String destinatario, String asunto, String cuerpo, boolean html,
                                             List<EmailAdjunto> adjuntos,
                                             TipoEventoEmail tipoEvento, Long referenciaId) {
        return new SolicitudEmail(destinatario, asunto, cuerpo, html, adjuntos, tipoEvento, referenciaId);
    }
}
