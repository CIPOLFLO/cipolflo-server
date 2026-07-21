package com.cipolflo.server.ajustes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CostoCuotaRequestDto(
        @NotNull(message = "El monto de la cuota social es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El monto de la cuota social debe ser mayor a cero")
        BigDecimal monto
) {}
