package com.cipolflo.server.finanzas.dto;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FinanzaCrearRequestDto {

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La procedencia es obligatoria")
    private Procedencia procedencia;

    @NotNull(message = "El concepto es obligatorio")
    private Concepto concepto;

    private LocalDate fecha;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El importe debe ser mayor que cero")
    private BigDecimal importe;

    @NotNull(message = "La forma de pago es obligatoria")
    private FormaPago formaPago;

    private String notas;
}
