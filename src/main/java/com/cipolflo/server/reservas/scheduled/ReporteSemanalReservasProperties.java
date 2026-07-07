package com.cipolflo.server.reservas.scheduled;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del reporte semanal de reservas. Se bindea desde
 * {@code cipolflo.reportes.reservas-semanal.*} en application.properties.
 *
 * @param destinatario mail interno/administrativo al que se envía el reporte
 * @param cron         expresión cron (usada por el scheduler vía placeholder)
 * @param zona         zona horaria del cron (ej. America/Montevideo)
 */
@ConfigurationProperties(prefix = "cipolflo.reportes.reservas-semanal")
public record ReporteSemanalReservasProperties(
        String destinatario,
        String cron,
        String zona
) {
}
