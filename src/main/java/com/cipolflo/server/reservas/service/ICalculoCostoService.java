package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.dto.CalculoCostoRequestDto;
import com.cipolflo.server.reservas.dto.CalculoCostoResponseDto;
import com.cipolflo.server.servicios.domain.TarifaServicio;

public interface ICalculoCostoService {
    CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request);

    /**
     * Variante para cuando el llamador ya resolvió la TarifaServicio aplicable
     * (ej. ReservaCreacionValidator, al validar horas), evitando repetir la
     * resolución de cliente/tarifa dentro del mismo request.
     */
    CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request, TarifaServicio tarifaResuelta);
}
