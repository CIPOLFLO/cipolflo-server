package com.cipolflo.server.ajustes.mapper;

import com.cipolflo.server.ajustes.domain.CostoCuotaSocio;
import com.cipolflo.server.ajustes.dto.CostoCuotaResponseDto;

public class CostoCuotaMapper {

    private CostoCuotaMapper() {}

    public static CostoCuotaResponseDto toResponseDto(CostoCuotaSocio costoCuota) {
        return new CostoCuotaResponseDto(
                costoCuota.getMonto(),
                costoCuota.getUpdatedAt(),
                costoCuota.getUpdatedBy()
        );
    }
}
