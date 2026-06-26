package com.cipolflo.server.reservas;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import jakarta.annotation.Nullable;

public class ReservaMapper {
    public static ReservaDetalleResponseDto toDetalleResponseDto(
            Reserva reserva,
            @Nullable ClienteDetalleReservaDto cliente,
            ServicioDetalleReservaDto servicio
    ){
        return new ReservaDetalleResponseDto(
                reserva.getId(),
                reserva.getTipoReserva(),
                reserva.getEstado(),
                reserva.getProcedencia(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getCantidadTotal(),
                reserva.getCantidadMenores(),
                reserva.getCantidad(),
                reserva.getImporte(),
                reserva.getPago(),
                reserva.getRequiereDocumentacion(),
                reserva.getTieneDocumentacion(),
                reserva.getRut(),
                reserva.getNotas(),
                cliente,
                servicio,
                reserva.getCreatedAt(),
                reserva.getUpdatedAt(),
                reserva.getCreatedBy(),
                reserva.getUpdatedBy()
        );
    }
}
