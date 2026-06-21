package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PagoCuotaResponseDto(
        Long id,
        Long socioId,
        Integer anio,
        Integer mes,
        String nombreMes,
        String descripcion,
        Instant fechaPago,
        BigDecimal importe,
        MetodoCobro metodoCobro
) {}