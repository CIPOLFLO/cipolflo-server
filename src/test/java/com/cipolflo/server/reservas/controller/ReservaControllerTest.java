package com.cipolflo.server.reservas.controller;

import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.service.IReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReservaController.class,
        excludeAutoConfiguration = {HibernateJpaAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = true)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReservaService reservaService;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser
    void deberiaRetornarOkCuandoUsuarioEstaAutenticadoYReservaExiste() throws Exception {
        ReservaDetalleResponseDto response = mock(ReservaDetalleResponseDto.class);
        when(reservaService.getDetalle(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reservas/1"))
                .andExpect(status().isOk());

        verify(reservaService).getDetalle(1L);
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/reservas/1"))
                .andExpect(status().isUnauthorized());

        verify(reservaService, never()).getDetalle(anyLong());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoElIdEsCero() throws Exception {
        mockMvc.perform(get("/api/v1/reservas/0"))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).getDetalle(anyLong());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoElIdEsNegativo() throws Exception {
        mockMvc.perform(get("/api/v1/reservas/-5"))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).getDetalle(anyLong());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoReservaNoExiste() throws Exception {
        when(reservaService.getDetalle(99L))
                .thenThrow(new ReservaNotFoundException(99L));

        mockMvc.perform(get("/api/v1/reservas/99"))
                .andExpect(status().isNotFound());

        verify(reservaService).getDetalle(99L);
    }
}
