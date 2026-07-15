package com.cipolflo.server.reservas.domain.enums;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;

public enum PlazoConfirmacion {
    VEINTICUATRO_HORAS("24 horas", Duration.ofHours(24), Duration.ofHours(24)),
    TRES_MESES("3 meses", Period.ofMonths(3), Period.ofDays(7));

    private final String label;
    private final TemporalAmount offsetCancelacion; // antes del inicio de la reserva
    private final TemporalAmount offsetAlerta;      // antes de la fecha límite

    PlazoConfirmacion(String label, TemporalAmount offsetCancelacion, TemporalAmount offsetAlerta) {
        this.label = label;
        this.offsetCancelacion = offsetCancelacion;
        this.offsetAlerta = offsetAlerta;
    }

    public String getLabel() {
        return label;
    }

   
    public LocalDateTime calcularFechaLimiteConfirmacion(LocalDateTime inicioReserva) {
        return inicioReserva.minus(offsetCancelacion);
    }

 
    public LocalDateTime calcularFechaInicioAlerta(LocalDateTime fechaLimiteConfirmacion) {
        return fechaLimiteConfirmacion.minus(offsetAlerta);
    }

    @Override
    public String toString() {
        return label;
    }
}