package com.cipolflo.server.reservas.controller;

import com.cipolflo.server.reservas.dto.CalculoCostoResponseDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.service.ICancelacionReservaService;
import com.cipolflo.server.reservas.service.IFinalizacionReservaService;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReservaController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = true)
class CalculoCostoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReservaService reservaService;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockitoBean
    private ICancelacionReservaService cancelacionReservaService;
    @MockitoBean
    private IFinalizacionReservaService finalizacionReservaService;

    private static final String URL = "/api/v1/reservas/calcular-costo";

    private static final String BODY_MINIMO = """
            {
                "servicioId": 1,
                "fechaInicio": "2026-07-01",
                "fechaFin": "2026-07-03"
            }
            """;

    @Test
    void deberiaRetornarUnauthorizedSinAutenticacion() throws Exception {
        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_MINIMO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deberiaRetornarOkConBodyMinimo() throws Exception {
        when(reservaService.calcularCosto(any())).thenReturn(new CalculoCostoResponseDto(new BigDecimal("3000")));

        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_MINIMO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costoTotal").value(3000));
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoServicioNoExiste() throws Exception {
        when(reservaService.calcularCosto(any())).thenThrow(new ServicioNotFoundException(99L));

        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "servicioId": 99,
                                    "fechaInicio": "2026-07-01",
                                    "fechaFin": "2026-07-03"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFechaFinEsAnteriorAInicio() throws Exception {
        when(reservaService.calcularCosto(any())).thenThrow(
                new ReservaValidacionException(ReservaCodigoError.FECHA_FIN_ANTERIOR_A_INICIO,
                        "La fecha de fin no puede ser anterior a la fecha de inicio"));

        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "servicioId": 1,
                                    "fechaInicio": "2026-07-05",
                                    "fechaFin": "2026-07-01"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoPorHoraSinHoras() throws Exception {
        when(reservaService.calcularCosto(any())).thenThrow(
                new ReservaValidacionException(ReservaCodigoError.HORA_REQUERIDA_PARA_SERVICIO_POR_HORA,
                        "Las horas de inicio y fin son obligatorias para servicios con modalidad POR_HORA"));

        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_MINIMO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaServicioId() throws Exception {
        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fechaInicio": "2026-07-01",
                                    "fechaFin": "2026-07-03"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaFechaInicio() throws Exception {
        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "servicioId": 1,
                                    "fechaFin": "2026-07-03"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCantidadEsNegativa() throws Exception {
        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "servicioId": 1,
                                    "fechaInicio": "2026-07-01",
                                    "fechaFin": "2026-07-03",
                                    "cantidad": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaAceptarTodosLosParametrosOpcionales() throws Exception {
        when(reservaService.calcularCosto(any())).thenReturn(new CalculoCostoResponseDto(new BigDecimal("5200")));

        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "servicioId": 1,
                                    "fechaInicio": "2026-07-01",
                                    "fechaFin": "2026-07-03",
                                    "cantidadTotal": 6,
                                    "cantidadMenores": 1,
                                    "tipoCliente": "SOCIO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costoTotal").value(5200));
    }
}
