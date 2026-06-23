package com.cipolflo.server.finanzas.dto;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;

import java.time.LocalDate;

public record ListadoFinanzasRequestDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Concepto concepto,
        TipoMovimiento tipoMovimiento
) {
}