package com.cipolflo.server.finanzas.service;

import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.dto.FinanzaDetalleResponseDto;
import com.cipolflo.server.finanzas.dto.FinanzaResponseDto;

public interface IFinanzaService {

    FinanzaResponseDto registrarFinanza(FinanzaCrearRequestDto dto);

    FinanzaDetalleResponseDto getDetalleFinanza(Long id);
}
