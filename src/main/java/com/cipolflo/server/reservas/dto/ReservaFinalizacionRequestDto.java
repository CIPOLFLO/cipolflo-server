package com.cipolflo.server.reservas.dto;


import com.cipolflo.server.shared.enums.FormaPago;
import jdk.jfr.Description;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservaFinalizacionRequestDto {

    @Description(value = "Indica si se debe completar el pago pendiente antes de finalizar la reserva")
    private Boolean completarPago;

    @Description(value = "Forma de pago utilizada para completar el saldo pendiente")
    private FormaPago formaPago;

    @Description(value = "Notas opcionales del pago al finalizar la reserva")
    private String notas;
}