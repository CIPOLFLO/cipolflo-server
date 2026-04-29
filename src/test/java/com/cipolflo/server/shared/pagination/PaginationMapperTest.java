package com.cipolflo.server.shared.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PaginationMapperTest {
    @Test
    void deberiaConvertirPageEnPageResponse() {
        Page<String> page = new PageImpl<>(
                List.of("dato1", "dato2"),
                PageRequest.of(0, 2),
                5
        );

        PageResponse<String> response = PaginationMapper.toPageResponse(page);

        assertEquals(List.of("dato1", "dato2"), response.content());
        assertEquals(0, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
        assertTrue(response.first());
        assertFalse(response.last());
    }
}
