package com.cipolflo.server.ajustes.controller;

import com.cipolflo.server.ajustes.dto.CostoCuotaResponseDto;
import com.cipolflo.server.ajustes.service.ICostoCuotaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(CostoCuotaController.class)
class CostoCuotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICostoCuotaService costoCuotaService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private CostoCuotaResponseDto respuesta(String monto) {
        return new CostoCuotaResponseDto(new BigDecimal(monto), Instant.parse("2026-01-01T00:00:00Z"), "admin@cipolflo.com");
    }

    @Test
    @WithMockUser
    void obtenerCostoCuota_devuelveElValorVigente() throws Exception {
        when(costoCuotaService.obtenerCostoCuota()).thenReturn(respuesta("200.00"));

        mockMvc.perform(get("/api/v1/ajustes/costo-cuota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value(200.00));
    }

    @Test
    void obtenerCostoCuota_sinAutenticacion_retornaUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/ajustes/costo-cuota"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void actualizarCostoCuota_conMontoValido_retornaOk() throws Exception {
        when(costoCuotaService.actualizarCostoCuota(any())).thenReturn(respuesta("300.00"));

        mockMvc.perform(put("/api/v1/ajustes/costo-cuota")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 300.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value(300.00));
    }

    @Test
    @WithMockUser
    void actualizarCostoCuota_conMontoNulo_retornaBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/ajustes/costo-cuota")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(costoCuotaService, never()).actualizarCostoCuota(any());
    }

    @Test
    @WithMockUser
    void actualizarCostoCuota_conMontoCero_retornaBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/ajustes/costo-cuota")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(costoCuotaService, never()).actualizarCostoCuota(any());
    }

    @Test
    @WithMockUser
    void actualizarCostoCuota_conMontoNegativo_retornaBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/ajustes/costo-cuota")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": -50}"))
                .andExpect(status().isBadRequest());

        verify(costoCuotaService, never()).actualizarCostoCuota(any());
    }
}
