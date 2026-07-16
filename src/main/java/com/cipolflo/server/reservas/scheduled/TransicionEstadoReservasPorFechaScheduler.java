package com.cipolflo.server.reservas.scheduled;

import com.cipolflo.server.reservas.service.ITransicionEstadoReservasPorFechaService;
import com.cipolflo.server.shared.scheduling.EjecutorTareaProgramada;
import com.cipolflo.server.shared.scheduling.TipoTareaProgramada;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara la transición diaria de estados de reservas por fecha. Por defecto corre a
 * las 00:00 (hora de Uruguay); el cron y la zona se configuran en application.properties
 * ({@code cipolflo.tareas.transicion-estado-reservas.cron} / {@code .zona}).
 *
 * Cáscara delgada: solo agenda y delega en el ejecutor, que cronometra, registra el
 * resultado en {@code log_tareas_programadas} y contiene los errores.
 */
@Component
public class TransicionEstadoReservasPorFechaScheduler {

    private final ITransicionEstadoReservasPorFechaService transicionService;
    private final EjecutorTareaProgramada ejecutor;

    public TransicionEstadoReservasPorFechaScheduler(ITransicionEstadoReservasPorFechaService transicionService,
                                                      EjecutorTareaProgramada ejecutor) {
        this.transicionService = transicionService;
        this.ejecutor = ejecutor;
    }

    @Scheduled(
            // Default "-" (cron deshabilitado de Spring) si no hay config, para no romper
            // contextos sin estas properties (ej. tests). En prod manda application.properties.
            cron = "${cipolflo.tareas.transicion-estado-reservas.cron:-}",
            zone = "${cipolflo.tareas.transicion-estado-reservas.zona:America/Montevideo}"
    )
    public void transicionarEstados() {
        ejecutor.ejecutar(TipoTareaProgramada.TRANSICION_ESTADO_RESERVAS_POR_FECHA,
                transicionService::transicionarEstadosPorFecha);
    }
}
