package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroPagoCuotaRequestDto(
        @NotNull(message = "La cantidad de cuotas es obligatoria")
        @Positive(message = "La cantidad de cuotas debe ser mayor que cero")
        @Max(value = 12, message = "La cantidad máxima de cuotas es 12")
        Integer cantidadCuotas,

        @NotNull(message = "El importe total es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El importe debe ser mayor que cero")
        BigDecimal importeTotal,

        @NotNull(message = "El método de cobro es obligatorio")
        MetodoCobro metodoCobro,

        @NotNull(message = "La fecha de pago es obligatoria")
        LocalDate fechaPago,

        String observaciones
) {}