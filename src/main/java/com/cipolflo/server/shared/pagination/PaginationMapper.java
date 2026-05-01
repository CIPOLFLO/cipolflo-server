package com.cipolflo.server.shared.pagination;

import com.cipolflo.server.shared.dto.ResponseDto;
import org.springframework.data.domain.Page;

public class PaginationMapper {
    private PaginationMapper() {
    }

    public static <T extends ResponseDto> PageResponse<T> toPageResponse(Page<T> page) {
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
