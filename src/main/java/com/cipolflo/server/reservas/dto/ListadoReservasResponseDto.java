package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.PlazoConfirmacion;
import com.cipolflo.server.shared.dto.ResponseDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ListadoReservasResponseDto implements ResponseDto {
    private final Long id;
    private final Long clienteId;
    private final String nombreCliente;
    private final Long servicioId;
    private final String servicioNombre;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final EstadoReserva estadoReserva;
    private final boolean requiereDocumentacion;
    private final boolean tieneDocumentacion;
    private final BigDecimal montoImpago;
    private final PlazoConfirmacion plazoConfirmacion;
    private final LocalDateTime fechaLimiteConfirmacion;
    private final LocalDateTime fechaInicioAlerta;
    private final boolean requiereSena;
    private final boolean pago;

    public ListadoReservasResponseDto(
            Long id,
            Long clienteId,
            String nombreCliente,
            Long servicioId,
            String servicioNombre,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            EstadoReserva estadoReserva,
            boolean requiereDocumentacion,
            boolean tieneDocumentacion,
            BigDecimal montoImpago,
            PlazoConfirmacion plazoConfirmacion,
            LocalDateTime fechaLimiteConfirmacion,
            LocalDateTime fechaInicioAlerta,
            boolean requiereSena,
            boolean pago
    ) {
        this.id = id;
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
        this.servicioId = servicioId;
        this.servicioNombre = servicioNombre;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.estadoReserva = estadoReserva;
        this.requiereDocumentacion = requiereDocumentacion;
        this.tieneDocumentacion = tieneDocumentacion;
        this.montoImpago = montoImpago;
        this.plazoConfirmacion = plazoConfirmacion;
        this.fechaLimiteConfirmacion = fechaLimiteConfirmacion;
        this.fechaInicioAlerta = fechaInicioAlerta;
        this.requiereSena = requiereSena;
        this.pago = pago;
    }
}