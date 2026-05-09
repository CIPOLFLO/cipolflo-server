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

    @Test
    void deberiaUsarValoresPorDefectoCuandoAmbosParametrosSonNull() {
        PageRequestDto dto = new PageRequestDto(null, null);

        assertEquals(0, dto.page());
        assertEquals(1, dto.size());
    }

    @Test
    void deberiaUsarPagePorDefectoCuandoEsNull() {
        PageRequestDto dto = new PageRequestDto(null, 20);

        assertEquals(0, dto.page());
        assertEquals(20, dto.size());
    }

    @Test
    void deberiaUsarSizePorDefectoCuandoEsNull() {
        PageRequestDto dto = new PageRequestDto(3, null);

        assertEquals(3, dto.page());
        assertEquals(1, dto.size());
    }
}
