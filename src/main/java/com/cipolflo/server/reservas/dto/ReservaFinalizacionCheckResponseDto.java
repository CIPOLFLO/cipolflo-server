package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.shared.dto.ResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ReservaFinalizacionCheckResponseDto(
        @Schema(description = "Indica si la reserva puede finalizarse sin registrar un pago (ya está paga)")
        boolean puedeFinalizarSinPago,
        @Schema(description = "Monto que falta pagar de la reserva")
        BigDecimal montoImpago
) implements ResponseDto {
}
