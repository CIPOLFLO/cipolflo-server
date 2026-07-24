package com.cipolflo.server.ajustes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AntiguedadReservasRequestDto(
        @NotNull(message = "La antigüedad de reservas (en años) es obligatoria")
        @Positive(message = "La antigüedad de reservas debe ser un número de años positivo")
        Integer anios
) {}
