package com.cipolflo.server.shared.scheduling;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Persiste el log de una ejecución de tarea. Separado de {@link EjecutorTareaProgramada}
 * para que el {@code @Transactional(REQUIRES_NEW)} funcione vía proxy: el registro se
 * commitea en su propia transacción, aunque la tarea haya hecho rollback.
 */
@Component
public class LogTareaProgramadaRegistrar {

    private final LogTareaProgramadaRepository repository;

    public LogTareaProgramadaRegistrar(LogTareaProgramadaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(TipoTareaProgramada tarea, EstadoEjecucionTarea estado,
                          Instant inicio, Instant fin, String resumen, String error) {
        LogTareaProgramada log = new LogTareaProgramada();
        log.setTarea(tarea);
        log.setEstado(estado);
        log.setInicio(inicio);
        log.setFin(fin);
        log.setDuracionMs(Duration.between(inicio, fin).toMillis());
        log.setResumen(resumen);
        log.setError(error);
        repository.save(log);
    }
}
