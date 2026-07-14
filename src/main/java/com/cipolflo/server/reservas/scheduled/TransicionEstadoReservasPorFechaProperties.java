package com.cipolflo.server.reservas.scheduled;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la transición de estados de reservas por fecha. Se bindea desde
 * {@code cipolflo.tareas.transicion-estado-reservas.*} en application.properties.
 *
 * @param cron expresión cron (usada por el scheduler vía placeholder)
 * @param zona zona horaria del cron (ej. America/Montevideo)
 */
@ConfigurationProperties(prefix = "cipolflo.tareas.transicion-estado-reservas")
public record TransicionEstadoReservasPorFechaProperties(
        String cron,
        String zona
) {
}
