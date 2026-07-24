package com.cipolflo.server.ajustes.dto;

import com.cipolflo.server.shared.dto.ResponseDto;

import java.time.Instant;

public record AntiguedadReservasResponseDto(
        Integer anios,
        Instant updatedAt,
        String updatedBy
) implements ResponseDto {}
