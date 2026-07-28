package com.cipolflo.server.ajustes.dto;

import jakarta.validation.constraints.Size;

public record ListadoDestinatariosNotificacionEmailRequestDto(
        @Size(max = 100, message = "El alias no puede superar los 100 caracteres")
        String alias,
        Boolean activo
) {}
