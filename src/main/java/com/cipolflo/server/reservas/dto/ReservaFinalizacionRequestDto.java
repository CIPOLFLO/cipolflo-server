package com.cipolflo.server.reservas.dto;


import com.cipolflo.server.shared.enums.FormaPago;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservaFinalizacionRequestDto {

    @Schema(description = "Indica si se debe completar el pago pendiente antes de finalizar la reserva")
    private Boolean completarPago;

    @Schema(description = "Forma de pago utilizada para completar el saldo pendiente")
    private FormaPago formaPago;

    @Schema(description = "Notas opcionales del pago al finalizar la reserva")
    private String notas;
}
