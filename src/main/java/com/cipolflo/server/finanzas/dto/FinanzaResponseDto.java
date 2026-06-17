package com.cipolflo.server.finanzas.dto;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.shared.dto.ResponseDto;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FinanzaResponseDto implements ResponseDto {

    private Long id;
    private TipoMovimiento tipoMovimiento;
    private Procedencia procedencia;
    private Concepto concepto;
    private LocalDate fecha;
    private BigDecimal importe;
    private FormaPago formaPago;
    private String notas;
    private Long reservaId;
    private Long pagoCuotaId;
}