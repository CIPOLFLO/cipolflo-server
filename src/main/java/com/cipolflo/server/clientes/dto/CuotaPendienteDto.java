package com.cipolflo.server.clientes.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CuotaPendienteDto(
        Integer anio,
        Integer mes,
        String nombreMes
) {}