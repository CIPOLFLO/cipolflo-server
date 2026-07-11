package com.cipolflo.server.shared.mantenimiento;

import com.cipolflo.server.shared.scheduling.EjecutorTareaProgramada;
import com.cipolflo.server.shared.scheduling.TipoTareaProgramada;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara la purga de logs de envío de emails. Por defecto los domingos a las 05:00
 * (hora de Uruguay); el cron y la zona se configuran en application.properties
 * ({@code cipolflo.tareas.limpieza-logs-email.cron} / {@code .zona}).
 *
 * Cáscara delgada: solo agenda y delega en el ejecutor, que cronometra, registra el
 * resultado en {@code log_tareas_programadas} y contiene los errores. Otras limpiezas
 * van en sus propios schedulers, con su propio cron.
 */
@Component
public class LimpiezaLogsEmailScheduler {

    private final ILimpiezaLogsEmailService limpiezaLogsEmailService;
    private final EjecutorTareaProgramada ejecutor;

    public LimpiezaLogsEmailScheduler(ILimpiezaLogsEmailService limpiezaLogsEmailService,
                                      EjecutorTareaProgramada ejecutor) {
        this.limpiezaLogsEmailService = limpiezaLogsEmailService;
        this.ejecutor = ejecutor;
    }

    @Scheduled(
            // Default "-" (cron deshabilitado de Spring) si no hay config, para no romper
            // contextos sin estas properties (ej. tests). En prod manda application.properties.
            cron = "${cipolflo.tareas.limpieza-logs-email.cron:-}",
            zone = "${cipolflo.tareas.limpieza-logs-email.zona:America/Montevideo}"
    )
    public void purgarLogsVencidos() {
        ejecutor.ejecutar(TipoTareaProgramada.LIMPIEZA_LOGS_EMAIL, limpiezaLogsEmailService::purgarLogsVencidos);
    }
}
