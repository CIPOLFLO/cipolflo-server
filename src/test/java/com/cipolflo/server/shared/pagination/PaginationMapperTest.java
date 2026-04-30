package com.cipolflo.server.shared.pagination;

import com.cipolflo.server.shared.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginationMapperTest {

    @Test
    void deberiaConvertirPageEnPageResponse() {
        TestResponseDto dato1 = new TestResponseDto("dato1");
        TestResponseDto dato2 = new TestResponseDto("dato2");

        Page<TestResponseDto> page = new PageImpl<>(
                List.of(dato1, dato2),
                PageRequest.of(0, 2),
                5
        );

        PageResponse<TestResponseDto> response = PaginationMapper.toPageResponse(page);

        assertEquals(List.of(dato1, dato2), response.content());
        assertEquals(0, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
        assertTrue(response.first());
        assertFalse(response.last());
    }

    @Test
    void deberiaMapearPaginaVacia() {
        Page<TestResponseDto> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        PageResponse<TestResponseDto> response = PaginationMapper.toPageResponse(page);

        assertEquals(List.of(), response.content());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
    }

    @Test
    void deberiaIndicarUltimaPagina() {
        Page<TestResponseDto> page = new PageImpl<>(
                List.of(new TestResponseDto("dato1")),
                PageRequest.of(2, 2),
                5
        );

        PageResponse<TestResponseDto> response = PaginationMapper.toPageResponse(page);

        assertFalse(response.first());
        assertTrue(response.last());
    }

    private record TestResponseDto(String value) implements ResponseDto {
    }
}
