package com.cipolflo.server.ajustes.controller;

import com.cipolflo.server.ajustes.dto.DestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.service.IDestinatarioNotificacionEmailService;
import com.cipolflo.server.shared.email.exception.DestinatarioNotificacionEmailCodigoError;
import com.cipolflo.server.shared.email.exception.DestinatarioNotificacionEmailNoEncontradoException;
import com.cipolflo.server.shared.email.exception.DestinatarioNotificacionEmailValidacionException;
import com.cipolflo.server.shared.pagination.PageResponse;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DestinatarioNotificacionEmailController.class)
class DestinatarioNotificacionEmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDestinatarioNotificacionEmailService destinatarioNotificacionEmailService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private DestinatarioNotificacionEmailResponseDto detalle() {
        return new DestinatarioNotificacionEmailResponseDto(
                1L, "admin@cipolflo.com", "Administración", true,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
    }

    private PageResponse<ListadoDestinatarioNotificacionEmailResponseDto> paginaConResultados() {
        ListadoDestinatarioNotificacionEmailResponseDto dto = new ListadoDestinatarioNotificacionEmailResponseDto(
                1L, "admin@cipolflo.com", "Administración", true,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
        return new PageResponse<>(List.of(dto), 0, 10, 1, 1, true, true);
    }

    @Test
    @WithMockUser
    void getListado_sinFiltros_retornaOk() throws Exception {
        when(destinatarioNotificacionEmailService.getListado(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/ajustes/destinatarios-notificacion-email")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getListado_sinAutenticacion_retornaUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/ajustes/destinatarios-notificacion-email"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getDetalle_conIdExistente_retornaOk() throws Exception {
        when(destinatarioNotificacionEmailService.getDetalle(1L)).thenReturn(detalle());

        mockMvc.perform(get("/api/v1/ajustes/destinatarios-notificacion-email/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@cipolflo.com"));
    }

    @Test
    @WithMockUser
    void getDetalle_conIdInexistente_retornaNotFound() throws Exception {
        when(destinatarioNotificacionEmailService.getDetalle(99L))
                .thenThrow(new DestinatarioNotificacionEmailNoEncontradoException(99L));

        mockMvc.perform(get("/api/v1/ajustes/destinatarios-notificacion-email/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo")
                        .value(DestinatarioNotificacionEmailCodigoError.DESTINATARIO_NO_ENCONTRADO.name()));
    }

    @Test
    @WithMockUser
    void registrar_conDatosValidos_retornaCreated() throws Exception {
        when(destinatarioNotificacionEmailService.registrar(any())).thenReturn(detalle());

        mockMvc.perform(post("/api/v1/ajustes/destinatarios-notificacion-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"admin@cipolflo.com\", \"alias\": \"Administración\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin@cipolflo.com"));
    }

    @Test
    @WithMockUser
    void registrar_conEmailInvalido_retornaBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ajustes/destinatarios-notificacion-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"no-es-un-email\", \"alias\": \"Administración\"}"))
                .andExpect(status().isBadRequest());

        verify(destinatarioNotificacionEmailService, never()).registrar(any());
    }

    @Test
    @WithMockUser
    void registrar_conEmailDuplicado_retornaBadRequest() throws Exception {
        when(destinatarioNotificacionEmailService.registrar(any()))
                .thenThrow(new DestinatarioNotificacionEmailValidacionException(
                        DestinatarioNotificacionEmailCodigoError.EMAIL_DUPLICADO.name(),
                        "Ya existe un destinatario de notificaciones con ese email"));

        mockMvc.perform(post("/api/v1/ajustes/destinatarios-notificacion-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"admin@cipolflo.com\", \"alias\": \"Administración\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(DestinatarioNotificacionEmailCodigoError.EMAIL_DUPLICADO.name()));
    }

    @Test
    @WithMockUser
    void modificar_conDatosValidos_retornaOk() throws Exception {
        when(destinatarioNotificacionEmailService.modificar(eq(1L), any())).thenReturn(detalle());

        mockMvc.perform(put("/api/v1/ajustes/destinatarios-notificacion-email/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\": \"Tesorería\"}"))
                .andExpect(status().isOk());

        verify(destinatarioNotificacionEmailService).modificar(eq(1L), any());
    }

    @Test
    @WithMockUser
    void cambiarHabilitacion_conActivoValido_retornaOk() throws Exception {
        when(destinatarioNotificacionEmailService.cambiarHabilitacion(eq(1L), any())).thenReturn(detalle());

        mockMvc.perform(patch("/api/v1/ajustes/destinatarios-notificacion-email/1/habilitacion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\": false}"))
                .andExpect(status().isOk());

        verify(destinatarioNotificacionEmailService).cambiarHabilitacion(eq(1L), any());
    }

    @Test
    @WithMockUser
    void eliminar_conIdExistente_retornaNoContent() throws Exception {
        doNothing().when(destinatarioNotificacionEmailService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/ajustes/destinatarios-notificacion-email/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(destinatarioNotificacionEmailService).eliminar(1L);
    }

    @Test
    @WithMockUser
    void eliminar_conIdInexistente_retornaNotFound() throws Exception {
        doThrow(new DestinatarioNotificacionEmailNoEncontradoException(99L))
                .when(destinatarioNotificacionEmailService).eliminar(99L);

        mockMvc.perform(delete("/api/v1/ajustes/destinatarios-notificacion-email/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
