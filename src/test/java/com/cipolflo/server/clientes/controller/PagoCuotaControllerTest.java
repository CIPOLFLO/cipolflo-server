package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.dto.PagoCuotaResponseDto;
import com.cipolflo.server.clientes.dto.RegistroPagoCuotaRequestDto;
import com.cipolflo.server.clientes.service.IPagoCuotaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoCuotaController.class)
class PagoCuotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IPagoCuotaService pagoCuotaService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void deberiaRegistrarPagoCorrectamente() throws Exception {
        when(pagoCuotaService.registrarPago(eq(1L), any(RegistroPagoCuotaRequestDto.class)))
                .thenReturn(List.of(
                        new PagoCuotaResponseDto(
                                10L,
                                1L,
                                2026,
                                7,
                                "julio",
                                "Julio 2026",
                                Instant.parse("2026-06-15T03:00:00Z"),
                                BigDecimal.valueOf(5000),
                                MetodoCobro.EFECTIVO
                        )
                ));

        mockMvc.perform(post("/api/v1/clientes/socios/1/pago-cuota")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidadCuotas": 1,
                                  "importeTotal": 5000,
                                  "metodoCobro": "EFECTIVO",
                                  "fechaPago": "2026-06-15",
                                  "observaciones": "Pago en caja"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].socioId").value(1))
                .andExpect(jsonPath("$[0].anio").value(2026))
                .andExpect(jsonPath("$[0].mes").value(7))
                .andExpect(jsonPath("$[0].descripcion").value("Julio 2026"))
                .andExpect(jsonPath("$[0].metodoCobro").value("EFECTIVO"));

        verify(pagoCuotaService).registrarPago(eq(1L), any(RegistroPagoCuotaRequestDto.class));
    }

    @Test
    void deberiaRetornarBadRequestCuandoCantidadCuotasEsCero() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/socios/1/pago-cuota")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidadCuotas": 0,
                                  "importeTotal": 5000,
                                  "metodoCobro": "EFECTIVO",
                                  "fechaPago": "2026-06-15"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(pagoCuotaService, never()).registrarPago(any(), any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoCantidadCuotasSuperaDoce() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/socios/1/pago-cuota")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidadCuotas": 13,
                                  "importeTotal": 5000,
                                  "metodoCobro": "EFECTIVO",
                                  "fechaPago": "2026-06-15"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(pagoCuotaService, never()).registrarPago(any(), any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoImporteEsCero() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/socios/1/pago-cuota")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidadCuotas": 1,
                                  "importeTotal": 0,
                                  "metodoCobro": "EFECTIVO",
                                  "fechaPago": "2026-06-15"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(pagoCuotaService, never()).registrarPago(any(), any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoFaltaMetodoCobro() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/socios/1/pago-cuota")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidadCuotas": 1,
                                  "importeTotal": 5000,
                                  "fechaPago": "2026-06-15"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(pagoCuotaService, never()).registrarPago(any(), any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoFaltaFechaPago() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/socios/1/pago-cuota")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidadCuotas": 1,
                                  "importeTotal": 5000,
                                  "metodoCobro": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(pagoCuotaService, never()).registrarPago(any(), any());
    }

    @Test
    void deberiaRetornarUnauthorizedSinUsuarioAutenticado() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/socios/1/pago-cuota")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidadCuotas": 1,
                                  "importeTotal": 5000,
                                  "metodoCobro": "EFECTIVO",
                                  "fechaPago": "2026-06-15"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verify(pagoCuotaService, never()).registrarPago(any(), any());
    }
}