package com.cipolflo.server.ajustes.dto;

import jakarta.validation.constraints.NotNull;

public record HabilitacionClienteTelegramRequestDto(
        @NotNull(message = "El campo 'activo' es obligatorio")
        Boolean activo
) {}
