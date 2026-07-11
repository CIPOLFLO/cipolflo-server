package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.shared.enums.FormaPago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RegistroPagoReservaRequestDto {

    @NotNull
    @Positive
    @Schema(description = "Importe que se paga del total de la reserva")
    BigDecimal importe;

    @NotNull
    @Schema(description = "Indica si la reserva se debe considerar como paga o no, independientemente del importe")
    Boolean esPagoTotal;

    @NotNull
    @Schema(description = "Forma en que se realiza el pago")
    FormaPago formaPago;

    @Schema(description = "Notas opcionales acerca del pago realizado")
    String notas;

}
