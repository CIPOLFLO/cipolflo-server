package com.cipolflo.server.finanzas.dto;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FinanzaExportRequestDto {

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Concepto concepto;
    private TipoMovimiento tipoMovimiento;
}
