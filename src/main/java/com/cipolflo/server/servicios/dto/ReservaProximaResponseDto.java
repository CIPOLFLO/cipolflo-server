package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ReservaProximaResponseDto {
    private final Long id;
    private final Long clienteId;
    private final String nombreCliente;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final Boolean pago;
    private final EstadoReserva estado;

    public ReservaProximaResponseDto(
         Long id,
         Long clienteId,
         String nombreCliente,
         LocalDate fechaEntrada,
         LocalDate fechaSalida,
         Boolean pago,
         EstadoReserva estado
    ) {
        this.id = id;
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.pago = pago;
        this.estado = estado;
    }
}
