package com.cipolflo.server.finanzas.dto;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.shared.dto.ResponseDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class ListadoFinanzasResponseDto implements ResponseDto {

    private final Long id;
    private final Concepto concepto;
    private final LocalDate fecha;
    private final BigDecimal importe;
    private final String notas;
    private final TipoMovimiento tipoMovimiento;

    public ListadoFinanzasResponseDto(
            Long id,
            Concepto concepto,
            LocalDate fecha,
            BigDecimal importe,
            String notas,
            TipoMovimiento tipoMovimiento
    ) {
        this.id = id;
        this.concepto = concepto;
        this.fecha = fecha;
        this.importe = importe;
        this.notas = notas;
        this.tipoMovimiento = tipoMovimiento;
    }
}
