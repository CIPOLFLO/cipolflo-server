package com.cipolflo.server.reservas.dto;

import java.math.BigDecimal;

public record ReservaFinalizacionCheckResponseDto(
        boolean puedeFinalizarseDirectamente,
        BigDecimal montoImpago
) {
}