package com.cipolflo.server.servicios.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ServicioHabilitacionResponseDto {
    private final ServicioResponseDto servicio;
    private final List<ReservaProximaResponseDto> reservasProximas;
    private final String mensaje;

    public ServicioHabilitacionResponseDto(
            ServicioResponseDto servicio,
            List<ReservaProximaResponseDto> reservasProximas,
            String mensaje
    ) {
        this.servicio = servicio;
        this.reservasProximas = reservasProximas;
        this.mensaje = mensaje;
    }

}
