package com.cipolflo.server.shared.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageRequestDtoTest {

    @Test
    void deberiaConvertirDtoAPageable() {
        PageRequestDto dto = new PageRequestDto(2, 15, null, null);

        Pageable pageable = dto.toPageable();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());
    }

    @Test
    void deberiaUsarValoresPorDefectoCuandoAmbosParametrosSonNull() {
        PageRequestDto dto = new PageRequestDto(null, null, null, null);

        assertEquals(0, dto.page());
        assertEquals(1, dto.size());
    }

    @Test
    void deberiaUsarPagePorDefectoCuandoEsNull() {
        PageRequestDto dto = new PageRequestDto(null, 20, null, null);

        assertEquals(0, dto.page());
        assertEquals(20, dto.size());
    }

    @Test
    void deberiaUsarSizePorDefectoCuandoEsNull() {
        PageRequestDto dto = new PageRequestDto(3, null, null, null);

        assertEquals(3, dto.page());
        assertEquals(1, dto.size());
    }

    @Test
    void deberiaRetornarPageableSinSortCuandoSortFieldEsNull() {
        PageRequestDto dto = new PageRequestDto(0, 10, null, null);

        Pageable pageable = dto.toPageable();

        assertFalse(pageable.getSort().isSorted());
    }

    @Test
    void deberiaRetornarPageableConSortAscPorDefectoCuandoSoloSeSuministraSortField() {
        PageRequestDto dto = new PageRequestDto(0, 10, "nombreCompleto", null);

        Pageable pageable = dto.toPageable();

        assertTrue(pageable.getSort().isSorted());
        assertEquals(Sort.Direction.ASC,
                pageable.getSort().getOrderFor("nombreCompleto").getDirection());
    }

    @Test
    void deberiaRetornarPageableConSortDescCuandoSortOrderEsDESC() {
        PageRequestDto dto = new PageRequestDto(0, 10, "nombreCompleto", "DESC");

        Pageable pageable = dto.toPageable();

        assertTrue(pageable.getSort().isSorted());
        assertEquals(Sort.Direction.DESC,
                pageable.getSort().getOrderFor("nombreCompleto").getDirection());
    }

    @Test
    void deberiaRetornarPageableConSortAscCuandoSortOrderEsASC() {
        PageRequestDto dto = new PageRequestDto(0, 10, "cedula", "ASC");

        Pageable pageable = dto.toPageable();

        assertTrue(pageable.getSort().isSorted());
        assertEquals(Sort.Direction.ASC,
                pageable.getSort().getOrderFor("cedula").getDirection());
    }
}
