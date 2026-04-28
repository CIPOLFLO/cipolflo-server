package com.cipolflo.server.shared.pagination;

import org.junit.jupiter.api.Test;

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
}
