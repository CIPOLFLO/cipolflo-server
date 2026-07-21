package com.cipolflo.server.ajustes.mapper;

import com.cipolflo.server.ajustes.dto.AntiguedadReservasResponseDto;
import com.cipolflo.server.shared.scheduling.ConfiguracionTarea;

public class AntiguedadReservasMapper {

    private AntiguedadReservasMapper() {}

    public static AntiguedadReservasResponseDto toResponseDto(ConfiguracionTarea configuracion) {
        return new AntiguedadReservasResponseDto(
                configuracion.valorComoEntero(),
                configuracion.getUpdatedAt(),
                configuracion.getUpdatedBy()
        );
    }
}
