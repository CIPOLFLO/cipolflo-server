package com.cipolflo.server.reservas.mapper;

import java.util.List;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import jakarta.annotation.Nullable;

import static com.cipolflo.server.shared.export.ExportFormatter.formatearBooleano;
import static com.cipolflo.server.shared.export.ExportFormatter.orEmpty;

public class ReservaMapper {

    private ReservaMapper(){}

    public static ReservaDetalleResponseDto toDetalleResponseDto(
            Reserva reserva,
            @Nullable ClienteDetalleReservaDto cliente,
            ServicioDetalleReservaDto servicio
    )

    {
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
                reserva.getMontoImpago(),
                reserva.getPago(),
                reserva.getRequiereDocumentacion(),
                reserva.getTieneDocumentacion(),
                reserva.getRequiereSena(),
                reserva.getNotas(),
                cliente,
                servicio,
                reserva.getCreatedAt(),
                reserva.getUpdatedAt(),
                reserva.getCreatedBy(),
                reserva.getUpdatedBy()
        );
    }

    public static List<String> toExportFila(Reserva reserva, String nombreCliente, String nombreServicio){
        return List.of(
                orEmpty(reserva.getId()),
                orEmpty(reserva.getTipoReserva().toString()),
                orEmpty(reserva.getEstado().toString()),
                orEmpty(reserva.getProcedencia()),
                orEmpty(nombreServicio),
                orEmpty(nombreCliente),
                orEmpty(reserva.getFechaEntrada()),
                orEmpty(reserva.getFechaSalida()),
                orEmpty(reserva.getHoraInicio()),
                orEmpty(reserva.getHoraFin()),
                orEmpty(reserva.getImporte()),
                formatearBooleano(reserva.getPago()),
                orEmpty(reserva.getCantidadTotal()),
                orEmpty(reserva.getCantidadMenores()),
                orEmpty(reserva.getCantidad()),
                formatearBooleano(reserva.getRequiereDocumentacion()),
                formatearBooleano(reserva.getTieneDocumentacion()),
                orEmpty(reserva.getNotas())
        );
    }
}