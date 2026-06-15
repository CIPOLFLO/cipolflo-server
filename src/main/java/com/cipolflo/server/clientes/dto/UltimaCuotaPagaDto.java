package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;

import java.math.BigDecimal;
import java.time.Instant;

public record UltimaCuotaPagaDto(
        Integer anio,
        Integer mes,
        Instant fechaPago,
        BigDecimal importe,
        MetodoCobro metodoCobro
) {}
