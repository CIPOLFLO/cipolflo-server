package com.cipolflo.server.shared.scheduling;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Registro de una ejecución de tarea programada. Tabla append-only: una fila por corrida
 * (EXITO o FALLIDO), con el resumen de lo que hizo o el error. Lo escribe
 * {@link LogTareaProgramadaRegistrar} en su propia transacción.
 */
@Entity
@Table(name = "log_tareas_programadas")
@Getter
@Setter
public class LogTareaProgramada extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoTareaProgramada tarea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEjecucionTarea estado;

    @Column(nullable = false)
    private Instant inicio;

    @Column(nullable = false)
    private Instant fin;

    @Column(nullable = false)
    private long duracionMs;

    /** Resumen legible de lo que hizo la tarea (ej. "5 registros eliminados"). */
    @Column(columnDefinition = "TEXT")
    private String resumen;

    /** Detalle del error cuando el estado es FALLIDO. */
    @Column(columnDefinition = "TEXT")
    private String error;
}
