package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;

import java.time.LocalDate;

public record ServicioReservaOcupacionDto(
        Long reservaId,
        EstadoReserva estado,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
