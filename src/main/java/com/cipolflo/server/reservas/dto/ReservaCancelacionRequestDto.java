package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.shared.enums.FormaPago;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ReservaCancelacionRequestDto {

    @Schema(description = "Indica si se debe generar una devolución por los pagos asociados")
    private Boolean generarDevolucion;

    @Schema(description = "Forma de pago utilizada para registrar la devolución")
    private FormaPago formaPago;

    private BigDecimal importeDevolucion;

}
