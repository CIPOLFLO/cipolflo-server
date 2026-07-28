package com.cipolflo.server.reservas.scheduled;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del reporte semanal de reservas. Se bindea desde
 * {@code cipolflo.reportes.reservas-semanal.*} en application.properties.
 *
 * El destinatario ya no vive acá: se administra desde la pantalla de Ajustes
 * (ver {@link com.cipolflo.server.shared.email.IConsultaDestinatariosNotificacionEmail}).
 *
 * @param cron expresión cron (usada por el scheduler vía placeholder)
 * @param zona zona horaria del cron (ej. America/Montevideo)
 */
@ConfigurationProperties(prefix = "cipolflo.reportes.reservas-semanal")
public record ReporteSemanalReservasProperties(
        String cron,
        String zona
) {
}
