package com.cipolflo.server.servicios.mapper;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.servicios.dto.ServicioReservaOcupacionDto;

import java.util.List;

public class ServicioReservaOcupacionMapper {

    private ServicioReservaOcupacionMapper() {}

    public static ServicioReservaOcupacionDto toOcupacionDto(Reserva reserva) {
        return new ServicioReservaOcupacionDto(
                reserva.getId(),
                reserva.getEstado(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida()
        );
    }

    public static List<ServicioReservaOcupacionDto> toOcupacionDtoList(List<Reserva> reservas) {
        return reservas.stream()
                .map(ServicioReservaOcupacionMapper::toOcupacionDto)
                .toList();
    }
}
