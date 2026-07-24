package com.cipolflo.server.ajustes.controller;

import com.cipolflo.server.ajustes.dto.AntiguedadReservasResponseDto;
import com.cipolflo.server.ajustes.service.IAntiguedadReservasService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AntiguedadReservasController.class)
class AntiguedadReservasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAntiguedadReservasService antiguedadReservasService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private AntiguedadReservasResponseDto respuesta(int anios) {
        return new AntiguedadReservasResponseDto(anios, Instant.parse("2026-01-01T00:00:00Z"), "admin@cipolflo.com");
    }

    @Test
    @WithMockUser
    void obtenerAntiguedad_devuelveElValorVigente() throws Exception {
        when(antiguedadReservasService.obtenerAntiguedad()).thenReturn(respuesta(2));

        mockMvc.perform(get("/api/v1/ajustes/antiguedad-reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anios").value(2));
    }

    @Test
    void obtenerAntiguedad_sinAutenticacion_retornaUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/ajustes/antiguedad-reservas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void actualizarAntiguedad_conAniosValido_retornaOk() throws Exception {
        when(antiguedadReservasService.actualizarAntiguedad(any())).thenReturn(respuesta(5));

        mockMvc.perform(put("/api/v1/ajustes/antiguedad-reservas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anios\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anios").value(5));
    }

    @Test
    @WithMockUser
    void actualizarAntiguedad_conAniosNulo_retornaBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/ajustes/antiguedad-reservas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(antiguedadReservasService, never()).actualizarAntiguedad(any());
    }

    @Test
    @WithMockUser
    void actualizarAntiguedad_conAniosCero_retornaBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/ajustes/antiguedad-reservas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anios\": 0}"))
                .andExpect(status().isBadRequest());

        verify(antiguedadReservasService, never()).actualizarAntiguedad(any());
    }

    @Test
    @WithMockUser
    void actualizarAntiguedad_conAniosNegativo_retornaBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/ajustes/antiguedad-reservas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anios\": -3}"))
                .andExpect(status().isBadRequest());

        verify(antiguedadReservasService, never()).actualizarAntiguedad(any());
    }
}
