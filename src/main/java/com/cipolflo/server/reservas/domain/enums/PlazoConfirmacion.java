package com.cipolflo.server.reservas.domain.enums;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /**
     * Combina {@code fechaEntrada} con {@code horaInicio} (o medianoche si el servicio no es
     * por hora) para obtener el inicio de la reserva, y delega en {@link #calcularFechaLimiteConfirmacion(LocalDateTime)}.
     * Única fuente de esta combinación: la usan tanto {@code Reserva} como los validadores.
     */
    public LocalDateTime calcularFechaLimiteConfirmacion(LocalDate fechaEntrada, LocalTime horaInicio) {
        LocalDateTime inicioReserva = fechaEntrada.atTime(horaInicio != null ? horaInicio : LocalTime.MIDNIGHT);
        return calcularFechaLimiteConfirmacion(inicioReserva);
    }

 
    public LocalDateTime calcularFechaInicioAlerta(LocalDateTime fechaLimiteConfirmacion) {
        return fechaLimiteConfirmacion.minus(offsetAlerta);
    }

    @Override
    public String toString() {
        return label;
    }
}