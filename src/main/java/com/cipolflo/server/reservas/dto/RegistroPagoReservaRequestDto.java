package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.shared.enums.FormaPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jdk.jfr.Description;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class RegistroPagoReservaRequestDto {

    @NotNull
    @Positive
    @Description(value="Importe que se paga del total de la reserva")
    BigDecimal importe;

    @NotNull
    @Description(value="Indica si la reserva se debe considerar como paga o no, independientemente del importe")
    Boolean esPagoTotal;

    @NotNull
    @Description(value="Forma en que se realiza el pago")
    FormaPago formaPago;

    @Description(value="Notas opcionales acerca del pago realizado")
    String notas;

}
