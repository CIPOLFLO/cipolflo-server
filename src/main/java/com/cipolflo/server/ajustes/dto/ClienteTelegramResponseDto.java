package com.cipolflo.server.ajustes.dto;

import com.cipolflo.server.shared.dto.ResponseDto;

import java.time.Instant;

public record ClienteTelegramResponseDto(
        Long id,
        Long chatId,
        String alias,
        Boolean activo,
        Boolean recibeNotificaciones,
        Instant createdAt,
        Instant updatedAt
) implements ResponseDto {}
