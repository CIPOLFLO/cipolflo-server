package com.cipolflo.server.finanzas.dto;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.shared.dto.AuditInfoDto;
import com.cipolflo.server.shared.dto.ResponseDto;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
public class FinanzaDetalleResponseDto extends AuditInfoDto implements ResponseDto {

    private final Long id;
    private final TipoMovimiento tipoMovimiento;
    private final Procedencia procedencia;
    private final Concepto concepto;
    private final LocalDate fecha;
    private final BigDecimal importe;
    private final FormaPago formaPago;
    private final String notas;

    public FinanzaDetalleResponseDto(
            Long id,
            TipoMovimiento tipoMovimiento,
            Procedencia procedencia,
            Concepto concepto,
            LocalDate fecha,
            BigDecimal importe,
            FormaPago formaPago,
            String notas,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
        super(createdAt, updatedAt, createdBy, updatedBy);
        this.id = id;
        this.tipoMovimiento = tipoMovimiento;
        this.procedencia = procedencia;
        this.concepto = concepto;
        this.fecha = fecha;
        this.importe = importe;
        this.formaPago = formaPago;
        this.notas = notas;
    }
}
