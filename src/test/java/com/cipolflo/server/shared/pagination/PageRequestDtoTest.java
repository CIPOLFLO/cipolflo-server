package com.cipolflo.server.shared.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageRequestDtoTest {

    @Test
    void deberiaConvertirDtoAPageable() {
        PageRequestDto dto = new PageRequestDto(2, 15);

        Pageable pageable = dto.toPageable();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());
    }
}
