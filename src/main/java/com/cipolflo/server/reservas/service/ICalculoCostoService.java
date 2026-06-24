package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.dto.CalculoCostoRequestDto;
import com.cipolflo.server.reservas.dto.CalculoCostoResponseDto;

public interface ICalculoCostoService {
    CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request);
}
