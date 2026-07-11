package com.cipolflo.server.shared.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Ejecuta una tarea programada dejando trazado el resultado en {@code log_tareas_programadas}.
 *
 * Cronometra la corrida, registra EXITO con el resumen que devuelve la tarea, o FALLIDO con
 * el error si lanza excepción, y <b>no</b> propaga: así una falla queda registrada pero no
 * rompe el hilo del scheduler ni impide futuras ejecuciones. Los schedulers delegan aquí,
 * por lo que no necesitan su propio try/catch.
 */
@Component
public class EjecutorTareaProgramada {

    private static final Logger log = LoggerFactory.getLogger(EjecutorTareaProgramada.class);

    private final LogTareaProgramadaRegistrar registrar;

    public EjecutorTareaProgramada(LogTareaProgramadaRegistrar registrar) {
        this.registrar = registrar;
    }

    /**
     * Corre {@code accion} y persiste el resultado. La acción devuelve un resumen legible
     * de lo que hizo (ej. "5 registros eliminados"), que se guarda en el log.
     *
     * El registro de ÉXITO se hace fuera del try para no marcar como FALLIDO una tarea que
     * en realidad se ejecutó bien (solo falló el guardado del log). Y ambos registros se
     * escriben vía {@link #registrarSeguro}, que contiene sus propios errores: así una falla
     * al persistir el log nunca tumba el hilo del scheduler.
     */
    public void ejecutar(TipoTareaProgramada tarea, Supplier<String> accion) {
        log.info("Iniciando tarea programada: {}", tarea);
        Instant inicio = Instant.now();
        String resumen;
        try {
            resumen = accion.get();
        } catch (Exception e) {
            String detalle = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error("Tarea programada {} falló: {}", tarea, e.getMessage(), e);
            registrarSeguro(tarea, EstadoEjecucionTarea.FALLIDO, inicio, Instant.now(), null, detalle);
            return;
        }
        registrarSeguro(tarea, EstadoEjecucionTarea.EXITO, inicio, Instant.now(), resumen, null);
        log.info("Tarea programada {} finalizada OK: {}", tarea, resumen);
    }

    /**
     * Persiste el log de la corrida sin propagar: si el guardado falla, se loguea el error
     * pero no se relanza, para que el hilo del scheduler siga vivo pase lo que pase.
     */
    private void registrarSeguro(TipoTareaProgramada tarea, EstadoEjecucionTarea estado,
                                 Instant inicio, Instant fin, String resumen, String error) {
        try {
            registrar.registrar(tarea, estado, inicio, fin, resumen, error);
        } catch (Exception e) {
            log.error("No se pudo registrar el log de la tarea {} (estado {}): {}",
                    tarea, estado, e.getMessage(), e);
        }
    }
}
