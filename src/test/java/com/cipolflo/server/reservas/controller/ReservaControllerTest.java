package com.cipolflo.server.reservas.controller;

import com.cipolflo.server.reservas.dto.ListadoReservasRequestDto;
import com.cipolflo.server.reservas.dto.ListadoReservasResponseDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
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

import java.util.List;

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

    // ── GET /api/v1/reservas (listado) ─────────────────────────────────────────

    private PageResponse<ListadoReservasResponseDto> paginaVacia() {
        return new PageResponse<>(List.of(), 0, 10, 0, 0, true, true);
    }

    @Test
    void deberiaRetornarUnauthorizedAlListarSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/reservas"))
                .andExpect(status().isUnauthorized());

        verify(reservaService, never()).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarOkAlListarConUsuarioAutenticado() throws Exception {
        when(reservaService.getListadoReservas(
                any(ListadoReservasRequestDto.class), any(PageRequestDto.class)))
                .thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/reservas"))
                .andExpect(status().isOk());

        verify(reservaService).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestConSortFieldInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/reservas").param("sortField", "campoInvalido"))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarOkConSortFieldFechaEntrada() throws Exception {
        when(reservaService.getListadoReservas(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/reservas")
                        .param("sortField", "fechaEntrada")
                        .param("sortOrder", "ASC"))
                .andExpect(status().isOk());

        verify(reservaService).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarOkConSortFieldNombreCliente() throws Exception {
        when(reservaService.getListadoReservas(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/reservas")
                        .param("sortField", "nombreCliente")
                        .param("sortOrder", "DESC"))
                .andExpect(status().isOk());

        verify(reservaService).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestConServicioIdNegativo() throws Exception {
        mockMvc.perform(get("/api/v1/reservas").param("servicioId", "-1"))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestConNombreClienteDeMasDe100Caracteres() throws Exception {
        String nombreLargo = "a".repeat(101);

        mockMvc.perform(get("/api/v1/reservas").param("nombreCliente", nombreLargo))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestConEstadoReservaInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/reservas").param("estadoReserva", "INVALIDO"))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestConProcedenciaInvalida() throws Exception {
        mockMvc.perform(get("/api/v1/reservas").param("procedencia", "INVALIDO"))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).getListadoReservas(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaDelegarAlServiceConTodosLosFiltros() throws Exception {
        when(reservaService.getListadoReservas(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/reservas")
                        .param("procedencia", "CAMPING")
                        .param("servicioId", "1")
                        .param("nombreCliente", "Juan")
                        .param("estadoReserva", "PENDIENTE")
                        .param("fechaDesde", "2026-07-01")
                        .param("fechaHasta", "2026-07-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(reservaService).getListadoReservas(any(), any());
    }
}
