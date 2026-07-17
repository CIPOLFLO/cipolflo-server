package com.cipolflo.server.shared.mantenimiento;

import com.cipolflo.server.shared.scheduling.EjecutorTareaProgramada;
import com.cipolflo.server.shared.scheduling.TipoTareaProgramada;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara la limpieza de reservas viejas (con sus finanzas asociadas) y de finanzas sueltas
 * vencidas. Por defecto corre trimestralmente, el 1° de enero/abril/julio/octubre a las 03:00
 * (hora de Uruguay); el cron y la zona se configuran en application.properties
 * ({@code cipolflo.tareas.limpieza-reservas-finanzas.cron} / {@code .zona}). Los años de
 * retención se configuran aparte, en {@code configuracion_tarea}.
 *
 * Cáscara delgada: solo agenda y delega en el ejecutor, que cronometra, registra el
 * resultado en {@code log_tareas_programadas} y contiene los errores.
 */
@Component
public class LimpiezaReservasYFinanzasScheduler {

    private final ILimpiezaReservasYFinanzasService limpiezaService;
    private final EjecutorTareaProgramada ejecutor;

    public LimpiezaReservasYFinanzasScheduler(ILimpiezaReservasYFinanzasService limpiezaService,
                                              EjecutorTareaProgramada ejecutor) {
        this.limpiezaService = limpiezaService;
        this.ejecutor = ejecutor;
    }

    @Scheduled(
            // Default "-" (cron deshabilitado de Spring) si no hay config, para no romper
            // contextos sin estas properties (ej. tests). En prod manda application.properties.
            cron = "${cipolflo.tareas.limpieza-reservas-finanzas.cron:-}",
            zone = "${cipolflo.tareas.limpieza-reservas-finanzas.zona:America/Montevideo}"
    )
    public void limpiarVencidos() {
        ejecutor.ejecutar(TipoTareaProgramada.LIMPIEZA_RESERVAS_Y_FINANZAS, limpiezaService::limpiarVencidos);
    }
}
