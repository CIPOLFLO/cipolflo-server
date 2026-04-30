package com.cipolflo.server.shared.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record PageRequestDto(
        @PositiveOrZero(message = "page no puede ser negativo")
        int page,
        @Positive(message = "size debe ser mayor a 0")
        @Max(value = 100, message = "size no puede superar 100")
        int size
) {
    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
