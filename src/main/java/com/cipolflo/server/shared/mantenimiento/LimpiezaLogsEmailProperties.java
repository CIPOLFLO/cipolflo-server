package com.cipolflo.server.shared.mantenimiento;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la purga de logs de envío de emails. Se bindea desde
 * {@code cipolflo.tareas.limpieza-logs-email.*} en application.properties.
 *
 * @param cron          expresión cron (usada por el scheduler vía placeholder)
 * @param zona          zona horaria del cron (ej. America/Montevideo)
 * @param retencionDias antigüedad, en días, a partir de la cual se purgan los registros
 */
@ConfigurationProperties(prefix = "cipolflo.tareas.limpieza-logs-email")
public record LimpiezaLogsEmailProperties(
        String cron,
        String zona,
        int retencionDias
) {
}
