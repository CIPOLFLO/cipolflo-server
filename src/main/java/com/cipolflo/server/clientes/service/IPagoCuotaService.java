package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.dto.PagoCuotaResponseDto;
import com.cipolflo.server.clientes.dto.PeriodoCuotaDto;
import com.cipolflo.server.clientes.dto.RegistroPagoCuotaRequestDto;
import com.cipolflo.server.clientes.dto.UltimaCuotaDto;

import java.util.List;

public interface IPagoCuotaService {

    UltimaCuotaDto calcularUltimaCuotaPaga(Long socioId);

    List<PeriodoCuotaDto> calcularPeriodosCubiertos(Long socioId, Integer cantidadCuotas);

    List<PagoCuotaResponseDto> registrarPago(Long socioId, RegistroPagoCuotaRequestDto request);

    int calcularMesesAdeudados(Socio socio);
}