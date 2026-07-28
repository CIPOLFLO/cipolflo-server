package com.cipolflo.server.reservas.scheduled;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la cancelación automática de reservas pendientes vencidas. Se bindea
 * desde {@code cipolflo.tareas.cancelacion-automatica-reservas.*} en application.properties.
 *
 * @param cron expresión cron (usada por el scheduler vía placeholder)
 * @param zona zona horaria del cron (ej. America/Montevideo)
 */
@ConfigurationProperties(prefix = "cipolflo.tareas.cancelacion-automatica-reservas")
public record CancelacionAutomaticaReservasProperties(
        String cron,
        String zona
) {
}
