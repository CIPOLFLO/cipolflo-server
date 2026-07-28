package com.cipolflo.server.shared.email;

/**
 * Resuelve a qué direcciones de email se les envían las notificaciones administrativas
 * (reporte semanal de reservas, avisos de cancelación, etc.), configuradas desde la
 * pantalla de Ajustes en vez de una variable de entorno fija.
 */
public interface IConsultaDestinatariosNotificacionEmail {

    /**
     * @return los emails activos separados por coma (listos para {@code SolicitudEmail}),
     *         o {@code null} si no hay ninguno configurado
     */
    String destinatariosActivos();
}
