package com.cipolflo.server.shared.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageRequestDtoTest {

    @Test
    void deberiaUsarPaginaCeroSiLaPaginaEsNegativa() {
        PageRequestDto dto = new PageRequestDto(-1, 20);

        assertEquals(0, dto.page());
        assertEquals(20, dto.size());
    }

    @Test
    void deberiaUsarSizeDiezSiElSizeEsMenorOIgualACero() {
        PageRequestDto dto = new PageRequestDto(1, 0);

        assertEquals(1, dto.page());
        assertEquals(10, dto.size());
    }

    @Test
    void deberiaConvertirDtoAPageable() {
        PageRequestDto dto = new PageRequestDto(2, 15);

        Pageable pageable = dto.toPageable();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());
    }
}
