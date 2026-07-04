package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.shared.enums.FormaPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jdk.jfr.Description;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ReservaCancelacionRequestDto {

    @Description(value = "Indica si se debe generar una devolución por los pagos asociados")
    private Boolean generarDevolucion;

    @Description(value = "Forma de pago utilizada para registrar la devolución")
    private FormaPago formaPago;

    private BigDecimal importeDevolucion;

}
