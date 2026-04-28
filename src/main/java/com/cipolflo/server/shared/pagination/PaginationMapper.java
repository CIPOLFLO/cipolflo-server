package com.cipolflo.server.shared.pagination;

import org.springframework.data.domain.Page;

public class PaginationMapper {
    private PaginationMapper() {
    }

    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
