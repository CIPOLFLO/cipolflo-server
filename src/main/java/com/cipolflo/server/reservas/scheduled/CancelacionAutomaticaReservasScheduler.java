package com.cipolflo.server.reservas.scheduled;

import com.cipolflo.server.reservas.service.ICancelacionAutomaticaReservasService;
import com.cipolflo.server.shared.scheduling.EjecutorTareaProgramada;
import com.cipolflo.server.shared.scheduling.TipoTareaProgramada;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara la cancelación automática de reservas pendientes vencidas. Por defecto corre
 * todos los días a las 00:10 (hora de Uruguay); el cron y la zona se configuran en
 * application.properties ({@code cipolflo.tareas.cancelacion-automatica-reservas.cron} /
 * {@code .zona}).
 *
 * Cáscara delgada: solo agenda y delega en el ejecutor, que cronometra, registra el
 * resultado en {@code log_tareas_programadas} y contiene los errores.
 */
@Component
public class CancelacionAutomaticaReservasScheduler {

    private final ICancelacionAutomaticaReservasService cancelacionAutomaticaReservasService;
    private final EjecutorTareaProgramada ejecutor;

    public CancelacionAutomaticaReservasScheduler(ICancelacionAutomaticaReservasService cancelacionAutomaticaReservasService,
                                                  EjecutorTareaProgramada ejecutor) {
        this.cancelacionAutomaticaReservasService = cancelacionAutomaticaReservasService;
        this.ejecutor = ejecutor;
    }

    @Scheduled(
            cron = "${cipolflo.tareas.cancelacion-automatica-reservas.cron:-}",
            zone = "${cipolflo.tareas.cancelacion-automatica-reservas.zona:America/Montevideo}"
    )
    public void cancelarReservasVencidas() {
        ejecutor.ejecutar(TipoTareaProgramada.CANCELACION_AUTOMATICA_RESERVAS,
                cancelacionAutomaticaReservasService::cancelarReservasVencidas);
    }
}
