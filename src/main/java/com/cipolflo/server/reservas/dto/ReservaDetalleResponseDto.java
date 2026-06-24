package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.shared.dto.AuditInfoDto;
import com.cipolflo.server.shared.dto.ResponseDto;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ReservaDetalleResponseDto extends AuditInfoDto implements ResponseDto {

    private final Long id;
    private final TipoReserva tipoReserva;
    private final EstadoReserva estado;
    private final Procedencia procedencia;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final Integer cantidadTotal;
    private final Integer cantidadMenores;
    private final Integer cantidad;
    private final BigDecimal importe;
    private final FormaPago formaPago;
    private final Boolean pago;
    private final Boolean requiereDocumentacion;
    private final Boolean tieneDocumentacion;
    private final String rut;
    private final String notas;
    private final ClienteDetalleReservaDto cliente;
    private final ServicioDetalleReservaDto servicio;

    public ReservaDetalleResponseDto(
            Long id, TipoReserva tipoReserva, EstadoReserva estado, Procedencia procedencia,
            LocalDate fechaEntrada, LocalDate fechaSalida, LocalTime horaInicio, LocalTime horaFin,
            Integer cantidadTotal, Integer cantidadMenores, Integer cantidad,
            BigDecimal importe, FormaPago formaPago, Boolean pago,
            Boolean requiereDocumentacion, Boolean tieneDocumentacion,
            String rut, String notas,
            ClienteDetalleReservaDto cliente, ServicioDetalleReservaDto servicio,
            Instant createdAt, Instant updatedAt, String createdBy, String updatedBy) {
        super(createdAt, updatedAt, createdBy, updatedBy);
        this.id = id;
        this.tipoReserva = tipoReserva;
        this.estado = estado;
        this.procedencia = procedencia;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.cantidadTotal = cantidadTotal;
        this.cantidadMenores = cantidadMenores;
        this.cantidad = cantidad;
        this.importe = importe;
        this.formaPago = formaPago;
        this.pago = pago;
        this.requiereDocumentacion = requiereDocumentacion;
        this.tieneDocumentacion = tieneDocumentacion;
        this.rut = rut;
        this.notas = notas;
        this.cliente = cliente;
        this.servicio = servicio;
    }
}
