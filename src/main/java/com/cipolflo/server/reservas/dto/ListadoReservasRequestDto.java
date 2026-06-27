package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ListadoReservasRequestDto(
        Procedencia procedencia,
        @Min(0)
        Long servicioId,
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombreCliente,
        EstadoReserva estadoReserva,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {
}
