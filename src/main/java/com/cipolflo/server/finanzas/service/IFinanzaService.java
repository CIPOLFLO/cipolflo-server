package com.cipolflo.server.finanzas.service;

import com.cipolflo.server.finanzas.dto.*;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;

import java.math.BigDecimal;


public interface IFinanzaService {

    FinanzaResponseDto registrarFinanza(FinanzaCrearRequestDto dto);

    FinanzaDetalleResponseDto getDetalleFinanza(Long id);

    ArchivoExportado exportarFinanzas(ListadoFinanzasRequestDto filters);

    PageResponse<ListadoFinanzasResponseDto> getListadoFinanzas(
            ListadoFinanzasRequestDto filtros,
            PageRequestDto pageRequest
    );

    void registrarPagoReserva(FinanzaCrearRequestDto dto);

    void eliminarFinanza(Long id, boolean confirmar);

    void registrarPagoCuota(FinanzaCrearRequestDto dto);

    void registrarDevolucionPorCancelacionReserva(
            Long reservaId,
            BigDecimal importeDevolucion,
            FormaPago formaPago,
            Procedencia procedencia
    );
}
