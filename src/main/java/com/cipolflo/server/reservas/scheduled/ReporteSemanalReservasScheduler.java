package com.cipolflo.server.reservas.scheduled;

import com.cipolflo.server.reservas.service.IReporteSemanalReservasService;
import com.cipolflo.server.shared.scheduling.EjecutorTareaProgramada;
import com.cipolflo.server.shared.scheduling.TipoTareaProgramada;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara el envío del reporte semanal de reservas. Por defecto los lunes a las 08:00
 * (hora de Uruguay); el cron y la zona se configuran en application.properties
 * ({@code cipolflo.reportes.reservas-semanal.cron} / {@code .zona}).
 *
 * Cáscara delgada: solo agenda y delega en el ejecutor, que cronometra, registra el
 * resultado en {@code log_tareas_programadas} y contiene los errores.
 */
@Component
public class ReporteSemanalReservasScheduler {

    private final IReporteSemanalReservasService reporteService;
    private final EjecutorTareaProgramada ejecutor;

    public ReporteSemanalReservasScheduler(IReporteSemanalReservasService reporteService,
                                           EjecutorTareaProgramada ejecutor) {
        this.reporteService = reporteService;
        this.ejecutor = ejecutor;
    }

    @Scheduled(
            // Default "-" (cron deshabilitado de Spring) si no hay config, para no romper
            // contextos sin estas properties (ej. tests). En prod manda application.properties.
            cron = "${cipolflo.reportes.reservas-semanal.cron:-}",
            zone = "${cipolflo.reportes.reservas-semanal.zona:America/Montevideo}"
    )
    public void enviarReporteSemanal() {
        ejecutor.ejecutar(TipoTareaProgramada.REPORTE_SEMANAL_RESERVAS, reporteService::enviarReporteSemanal);
    }
}
