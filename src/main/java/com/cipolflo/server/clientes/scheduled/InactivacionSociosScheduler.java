package com.cipolflo.server.clientes.scheduled;

import com.cipolflo.server.clientes.service.IInactivacionSociosService;
import com.cipolflo.server.shared.scheduling.EjecutorTareaProgramada;
import com.cipolflo.server.shared.scheduling.TipoTareaProgramada;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InactivacionSociosScheduler {

    private final IInactivacionSociosService inactivacionSociosService;
    private final EjecutorTareaProgramada ejecutor;

    public InactivacionSociosScheduler(IInactivacionSociosService inactivacionSociosService,
                                       EjecutorTareaProgramada ejecutor) {
        this.inactivacionSociosService = inactivacionSociosService;
        this.ejecutor = ejecutor;
    }

    @Scheduled(
            // Default "-" (cron deshabilitado de Spring) si no hay config, para no romper
            // contextos sin estas properties (ej. tests). En prod manda application.properties.
            cron = "${cipolflo.tareas.inactivacion-socios.cron:-}",
            zone = "${cipolflo.tareas.inactivacion-socios.zona:America/Montevideo}"
    )
    public void inactivarSociosMorosos() {
        ejecutor.ejecutar(TipoTareaProgramada.INACTIVACION_SOCIOS, inactivacionSociosService::inactivarSociosMorosos);
    }
}
