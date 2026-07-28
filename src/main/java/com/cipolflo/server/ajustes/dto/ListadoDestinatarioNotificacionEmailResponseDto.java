package com.cipolflo.server.ajustes.dto;

import com.cipolflo.server.shared.dto.ResponseDto;

import java.time.Instant;

public record ListadoDestinatarioNotificacionEmailResponseDto(
        Long id,
        String email,
        String alias,
        Boolean activo,
        Instant createdAt,
        Instant updatedAt
) implements ResponseDto {}
