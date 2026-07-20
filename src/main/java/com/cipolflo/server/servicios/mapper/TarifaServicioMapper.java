package com.cipolflo.server.servicios.mapper;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.dto.TarifaServicioRequestDto;
import com.cipolflo.server.servicios.dto.TarifaServicioResponseDto;

public final class TarifaServicioMapper {

    private TarifaServicioMapper() {
    }

    public static TarifaServicio toEntity(
            Servicio servicio,
            TarifaServicioRequestDto dto
    ) {
        return TarifaServicio.registrar(
                servicio,
                dto.getTipoCliente(),
                dto.getPrecio(),
                dto.getModalidadPrecio(),
                dto.getAntiguedadMinima(),
                dto.getAntiguedadMaxima()
        );
    }

    public static TarifaServicioResponseDto toResponseDto(
            TarifaServicio tarifa
    ) {
        return new TarifaServicioResponseDto(
                tarifa.getId(),
                tarifa.getTipoCliente(),
                tarifa.getPrecio(),
                tarifa.getModalidadPrecio(),
                tarifa.getAntiguedadMinima(),
                tarifa.getAntiguedadMaxima()
        );
    }
}
