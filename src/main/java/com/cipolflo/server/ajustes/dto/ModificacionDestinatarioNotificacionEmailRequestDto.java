package com.cipolflo.server.ajustes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModificacionDestinatarioNotificacionEmailRequestDto(
        @NotBlank(message = "El alias es obligatorio")
        @Size(max = 100, message = "El alias no puede superar los 100 caracteres")
        String alias
) {}
