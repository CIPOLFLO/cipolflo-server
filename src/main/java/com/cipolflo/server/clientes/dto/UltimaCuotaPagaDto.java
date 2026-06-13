package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.shared.enums.FormaPago;

import java.math.BigDecimal;
import java.time.Instant;

public record UltimaCuotaPagaDto(
        Integer anio,
        Integer mes,
        Instant fechaPago,
        BigDecimal importe,
        FormaPago formaPago
) {}
