package com.cipolflo.server.shared.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record PageRequestDto(  int page,
                               @Positive(message = "size debe ser mayor a 0")
                               @Max(value = 100, message = "size no puede superar 100")
                               int size

){

    public PageRequestDto {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
    }
    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
