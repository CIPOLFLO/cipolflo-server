package com.cipolflo.server.reservas.controller;

import com.cipolflo.server.reservas.dto.*;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.service.ICancelacionReservaService;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.export.ArchivoExportado;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @MockitoBean
    private ICancelacionReservaService cancelacionReservaService;

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

    // ── PUT /api/v1/reservas/{id} (modificar) ─────────────────────────────────

    private String bodyValido() {
        return """
                {
                  "servicioId": 10,
                  "procedencia": "CAMPING",
                  "fechaInicio": "2026-07-01",
                  "fechaFin": "2026-07-05"
                }
                """;
    }

    @Test
    @WithMockUser
    void deberiaRetornarOkAlModificarReservaConUsuarioAutenticado() throws Exception {
        ReservaModificacionResponseDto response = mock(ReservaModificacionResponseDto.class);
        when(reservaService.modificar(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/reservas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservaService).modificar(eq(1L), any());
    }

    @Test
    void deberiaRetornarUnauthorizedAlModificarSinAutenticacion() throws Exception {
        mockMvc.perform(put("/api/v1/reservas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(reservaService, never()).modificar(anyLong(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlModificarConIdCero() throws Exception {
        mockMvc.perform(put("/api/v1/reservas/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido())
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).modificar(anyLong(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlModificarConIdNegativo() throws Exception {
        mockMvc.perform(put("/api/v1/reservas/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido())
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).modificar(anyLong(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundAlModificarReservaInexistente() throws Exception {
        when(reservaService.modificar(eq(99L), any()))
                .thenThrow(new ReservaNotFoundException(99L));

        mockMvc.perform(put("/api/v1/reservas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido())
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(reservaService).modificar(eq(99L), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlModificarSinServicioId() throws Exception {
        String body = """
                {
                  "procedencia": "CAMPING",
                  "fechaInicio": "2026-07-01",
                  "fechaFin": "2026-07-05"
                }
                """;

        mockMvc.perform(put("/api/v1/reservas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).modificar(anyLong(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlModificarSinFechaInicio() throws Exception {
        String body = """
                {
                  "servicioId": 10,
                  "procedencia": "CAMPING",
                  "fechaFin": "2026-07-05"
                }
                """;

        mockMvc.perform(put("/api/v1/reservas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).modificar(anyLong(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlModificarSinFechaFin() throws Exception {
        String body = """
                {
                  "servicioId": 10,
                  "procedencia": "CAMPING",
                  "fechaInicio": "2026-07-01"
                }
                """;

        mockMvc.perform(put("/api/v1/reservas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).modificar(anyLong(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlModificarConServicioIdCero() throws Exception {
        String body = """
                {
                  "servicioId": 0,
                  "procedencia": "CAMPING",
                  "fechaInicio": "2026-07-01",
                  "fechaFin": "2026-07-05"
                }
                """;

        mockMvc.perform(put("/api/v1/reservas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).modificar(anyLong(), any());
    }
    @Test
    @WithMockUser
    void deberiaExportarReservasCorrectamente() throws Exception {
    ArchivoExportado archivo = new ArchivoExportado(
            "reservas.xlsx",
            "excel".getBytes()
    );

    when(reservaService.exportarReservas(any())).thenReturn(archivo);

    mockMvc.perform(post("/api/v1/reservas/exportar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isOk())
            .andExpect(header().string(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"reservas.xlsx\""
            ));
    }

    @Test
    @WithMockUser
    void deberiaExportarReservasAunqueNoHayaResultados() throws Exception {
        ArchivoExportado archivo = new ArchivoExportado(
            "reservas.xlsx",
            new byte[0]
        );

        when(reservaService.exportarReservas(any())).thenReturn(archivo);

        mockMvc.perform(post("/api/v1/reservas/exportar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @WithMockUser
    void deberiaLlamarAlServiceAlExportarReservas() throws Exception {
        when(reservaService.exportarReservas(any()))
            .thenReturn(new ArchivoExportado("reservas.xlsx", new byte[0]));

        mockMvc.perform(post("/api/v1/reservas/exportar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isOk());

        verify(reservaService).exportarReservas(any());
    }

    @Test
    @WithMockUser
    void deberiaDevolverErrorSiFallaElServiceAlExportarReservas() throws Exception {
        when(reservaService.exportarReservas(any()))
            .thenThrow(new RuntimeException("Error exportando"));

        mockMvc.perform(post("/api/v1/reservas/exportar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().is5xxServerError());
    }
    @Test
    @WithMockUser
    void deberiaVerificarCancelacionSinPagos() throws Exception {
        ReservaCancelacionCheckResponseDto response = new ReservaCancelacionCheckResponseDto(
                true,
                List.of(),
                BigDecimal.ZERO
        );

        when(cancelacionReservaService.verificarCancelacion(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reservas/1/cancelacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedeCancelarseDirectamente").value(true))
                .andExpect(jsonPath("$.importeTotalPagos").value(0));

        verify(cancelacionReservaService).verificarCancelacion(1L);
    }

    @Test
    @WithMockUser
    void deberiaVerificarCancelacionConPagos() throws Exception {
        ReservaCancelacionCheckResponseDto response = new ReservaCancelacionCheckResponseDto(
                false,
                List.of(new PagoAsociadoReservaDto(
                        1L,
                        LocalDate.of(2026, 7, 1),
                        BigDecimal.valueOf(500),
                        FormaPago.EFECTIVO
                )),
                BigDecimal.valueOf(500)
        );

        when(cancelacionReservaService.verificarCancelacion(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reservas/1/cancelacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedeCancelarseDirectamente").value(false))
                .andExpect(jsonPath("$.pagosAsociados[0].id").value(1))
                .andExpect(jsonPath("$.importeTotalPagos").value(500));

        verify(cancelacionReservaService).verificarCancelacion(1L);
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundAlVerificarCancelacionDeReservaInexistente() throws Exception {
        when(cancelacionReservaService.verificarCancelacion(99L))
                .thenThrow(new ReservaNotFoundException(99L));

        mockMvc.perform(get("/api/v1/reservas/99/cancelacion"))
                .andExpect(status().isNotFound());

        verify(cancelacionReservaService).verificarCancelacion(99L);
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlVerificarCancelacionConReservaNoCancelable() throws Exception {
        when(cancelacionReservaService.verificarCancelacion(1L))
                .thenThrow(new ReservaValidacionException(
                        ReservaCodigoError.RESERVA_NO_CANCELABLE,
                        "No se puede cancelar una reserva en este estado"
                ));

        mockMvc.perform(get("/api/v1/reservas/1/cancelacion"))
                .andExpect(status().isBadRequest());

        verify(cancelacionReservaService).verificarCancelacion(1L);
    }

    @Test
    @WithMockUser
    void deberiaRetornarNoContentAlCancelarReserva() throws Exception {
        String body = """
            {
              "generarDevolucion": false
            }
            """;

        mockMvc.perform(patch("/api/v1/reservas/1/cancelacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(cancelacionReservaService).cancelar(eq(1L), any(ReservaCancelacionRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarNoContentAlCancelarReservaConDevolucion() throws Exception {
        String body = """
        {
          "generarDevolucion": true,
          "formaPago": "EFECTIVO",
          "importeDevolucion": 500
        }
        """;

        mockMvc.perform(patch("/api/v1/reservas/1/cancelacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(cancelacionReservaService).cancelar(eq(1L), any(ReservaCancelacionRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlCancelarConIdCero() throws Exception {
        mockMvc.perform(patch("/api/v1/reservas/0/cancelacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(cancelacionReservaService, never()).cancelar(anyLong(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundAlCancelarReservaInexistente() throws Exception {
        doThrow(new ReservaNotFoundException(99L))
                .when(cancelacionReservaService)
                .cancelar(eq(99L), any(ReservaCancelacionRequestDto.class));

        mockMvc.perform(patch("/api/v1/reservas/99/cancelacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"generarDevolucion\":false,\"importeDevolucion\":null}"))
                .andExpect(status().isNotFound());

        verify(cancelacionReservaService).cancelar(eq(99L), any(ReservaCancelacionRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestAlCancelarReservaNoCancelable() throws Exception {
        doThrow(new ReservaValidacionException(
                ReservaCodigoError.RESERVA_NO_CANCELABLE,
                "No se puede cancelar una reserva en este estado"
        )).when(cancelacionReservaService)
                .cancelar(eq(1L), any(ReservaCancelacionRequestDto.class));

        mockMvc.perform(patch("/api/v1/reservas/1/cancelacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"generarDevolucion\":false,\"importeDevolucion\":null}"))
                .andExpect(status().isBadRequest());

        verify(cancelacionReservaService).cancelar(eq(1L), any(ReservaCancelacionRequestDto.class));
    }

}
