package com.cipolflo.server.ajustes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistroClienteTelegramRequestDto(
        @NotNull(message = "El chatId es obligatorio")
        @Positive(message = "El chatId debe ser un número positivo")
        Long chatId,
        @NotBlank(message = "El alias es obligatorio")
        @Size(max = 100, message = "El alias no puede superar los 100 caracteres")
        String alias,
        Boolean recibeNotificaciones
) {
    public RegistroClienteTelegramRequestDto {
        if (recibeNotificaciones == null) recibeNotificaciones = true;
    }
}
