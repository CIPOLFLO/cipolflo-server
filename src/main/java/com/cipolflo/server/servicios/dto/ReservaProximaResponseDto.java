package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ReservaProximaResponseDto {
    private final Long id;
    private final Long clienteId;
    private final Instant fechaEntrada;
    private final Instant fechaSalida;
    private final Boolean pago;
    private final EstadoReserva estado;

    public ReservaProximaResponseDto(
         Long id,
         Long clienteId,
         Instant fechaEntrada,
         Instant fechaSalida,
         Boolean pago,
         EstadoReserva estado
    ) {
        this.id = id;
        this.clienteId = clienteId;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.pago = pago;
        this.estado = estado;
    }
}
