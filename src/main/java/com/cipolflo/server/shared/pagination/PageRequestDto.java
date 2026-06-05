package com.cipolflo.server.shared.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageRequestDto(
        @PositiveOrZero(message = "page no puede ser negativo")
        Integer page,
        @Positive(message = "size debe ser mayor a 0")
        @Max(value = 100, message = "size no puede superar 100")
        Integer size,
        String sortField,
        @Pattern(regexp = "ASC|DESC", message = "sortOrder debe ser ASC o DESC")
        String sortOrder
) {
    public PageRequestDto {
        if (page == null) page = 0;
        if (size == null) size = 1;
    }

    public Pageable toPageable() {
        if (sortField == null || sortField.isBlank())
            return PageRequest.of(page, size);
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}
