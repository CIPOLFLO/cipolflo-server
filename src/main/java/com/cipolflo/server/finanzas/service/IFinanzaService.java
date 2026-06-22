package com.cipolflo.server.finanzas.service;

import com.cipolflo.server.finanzas.dto.*;
import com.cipolflo.server.shared.export.ArchivoExportado;


public interface IFinanzaService {

    FinanzaResponseDto registrarFinanza(FinanzaCrearRequestDto dto);

    FinanzaDetalleResponseDto getDetalleFinanza(Long id);

    ArchivoExportado exportarFinanzas(FinanzaExportRequestDto filtros);


}
