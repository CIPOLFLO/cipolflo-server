package com.cipolflo.server.reservas.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReservaCancelacionCheckResponseDto(
        boolean puedeCancelarseDirectamente,
        List<PagoAsociadoReservaDto> pagosAsociados,
        BigDecimal importeTotalPagos
) {}
