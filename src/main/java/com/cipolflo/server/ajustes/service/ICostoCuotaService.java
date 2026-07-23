package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.CostoCuotaRequestDto;
import com.cipolflo.server.ajustes.dto.CostoCuotaResponseDto;

public interface ICostoCuotaService {

    CostoCuotaResponseDto obtenerCostoCuota();

    CostoCuotaResponseDto actualizarCostoCuota(CostoCuotaRequestDto dto);
}
