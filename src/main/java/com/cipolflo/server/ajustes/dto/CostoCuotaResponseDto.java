package com.cipolflo.server.ajustes.dto;

import com.cipolflo.server.shared.dto.ResponseDto;

import java.math.BigDecimal;
import java.time.Instant;

public record CostoCuotaResponseDto(
        BigDecimal monto,
        Instant updatedAt,
        String updatedBy
) implements ResponseDto {}
