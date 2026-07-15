package com.cipolflo.server.shared.mantenimiento;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de cuándo corre la limpieza de reservas viejas y finanzas sueltas vencidas.
 * Se bindea desde {@code cipolflo.tareas.limpieza-reservas-finanzas.*} en application.properties.
 * <p>
 * Los años de retención NO están acá: se leen de {@code configuracion_tarea} (ver
 * {@link com.cipolflo.server.shared.scheduling.ClaveConfiguracionTarea}) para que se puedan
 * ajustar sin necesidad de un deploy.
 *
 * @param cron expresión cron (usada por el scheduler vía placeholder)
 * @param zona zona horaria del cron (ej. America/Montevideo)
 */
@ConfigurationProperties(prefix = "cipolflo.tareas.limpieza-reservas-finanzas")
public record LimpiezaReservasYFinanzasProperties(
        String cron,
        String zona
) {
}
