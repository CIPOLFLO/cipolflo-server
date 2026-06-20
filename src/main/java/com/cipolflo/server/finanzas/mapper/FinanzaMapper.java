package com.cipolflo.server.finanzas.mapper;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.FinanzaDetalleResponseDto;
import com.cipolflo.server.finanzas.dto.FinanzaResponseDto;

public class FinanzaMapper {

    private FinanzaMapper() {}

    public static FinanzaResponseDto toResponseDto(Finanza finanza) {
        Ingreso ingreso = finanza instanceof Ingreso i ? i : null;

        return new FinanzaResponseDto(
                finanza.getId(),
                finanza instanceof Egreso ? TipoMovimiento.EGRESO : TipoMovimiento.INGRESO,
                finanza.getProcedencia(),
                finanza.getConcepto(),
                finanza.getFecha(),
                finanza.getImporte(),
                finanza.getFormaPago(),
                finanza.getNotas(),
                ingreso != null ? ingreso.getReservaId() : null,
                ingreso != null ? ingreso.getPagoCuotaId() : null
        );
    }
    public static FinanzaDetalleResponseDto toDetalleResponseDto(Finanza finanza) {
        return new FinanzaDetalleResponseDto(
                finanza.getId(),
                finanza.getTipoMovimiento(),
                finanza.getProcedencia(),
                finanza.getConcepto(),
                finanza.getFecha(),
                finanza.getImporte(),
                finanza.getFormaPago(),
                finanza.getNotas(),
                finanza.getCreatedAt(),
                finanza.getUpdatedAt(),
                finanza.getCreatedBy(),
                finanza.getUpdatedBy()
        );
    }
}