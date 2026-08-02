package com.cipolflo.server.manuales.controller;

import com.cipolflo.server.manuales.domain.CategoriaManual;
import com.cipolflo.server.manuales.domain.Manual;
import com.cipolflo.server.manuales.dto.ManualResponseDto;
import com.cipolflo.server.manuales.exception.ManualNoDisponibleException;
import com.cipolflo.server.manuales.exception.ManualNoEncontradoException;
import com.cipolflo.server.manuales.service.IManualService;
import com.cipolflo.server.manuales.service.ManualDescarga;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManualController.class)
class ManualControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IManualService manualService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private ManualResponseDto publicado() {
        return new ManualResponseDto(
                Manual.CLIENTES.getClave(),
                Manual.CLIENTES.getTitulo(),
                CategoriaManual.USUARIO,
                true,
                "1.0",
                LocalDate.of(2026, 7, 31));
    }

    /**
     * Manual sin PDF publicado. Hoy el catálogo está completo, así que ningún manual real
     * está en este estado: el escenario se simula sobre el service mockeado para cubrir el
     * contrato del controller (listado con {@code disponible: false} y 404 en la descarga),
     * que sigue vigente si un PDF no llega al build.
     */
    private ManualResponseDto pendiente() {
        return new ManualResponseDto(
                Manual.FINANZAS.getClave(),
                Manual.FINANZAS.getTitulo(),
                CategoriaManual.USUARIO,
                false,
                null,
                null);
    }

    private ManualDescarga descarga() {
        return new ManualDescarga(
                Manual.CLIENTES,
                new ByteArrayResource("%PDF-1.4".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @WithMockUser
    void listarManuales_devuelveElCatalogoConVersionYFecha() throws Exception {
        when(manualService.listarManuales(isNull())).thenReturn(List.of(publicado(), pendiente()));

        mockMvc.perform(get("/api/v1/manuales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clave").value("clientes"))
                .andExpect(jsonPath("$[0].titulo").value("Manual de Módulo Clientes"))
                .andExpect(jsonPath("$[0].disponible").value(true))
                .andExpect(jsonPath("$[0].version").value("1.0"))
                .andExpect(jsonPath("$[0].fechaActualizacion").value("2026-07-31"));
    }

    @Test
    @WithMockUser
    void listarManuales_manualSinPublicar_vieneSinVersion() throws Exception {
        when(manualService.listarManuales(isNull())).thenReturn(List.of(pendiente()));

        mockMvc.perform(get("/api/v1/manuales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].disponible").value(false))
                .andExpect(jsonPath("$[0].version").doesNotExist());
    }

    @Test
    @WithMockUser
    void listarManuales_conCategoria_filtra() throws Exception {
        when(manualService.listarManuales(CategoriaManual.TECNICO)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/manuales").param("categoria", "TECNICO"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void listarManuales_conCategoriaInvalida_retornaBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/manuales").param("categoria", "INVENTADA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarManuales_sinAutenticacion_retornaUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/manuales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void descargarManual_devuelveElPdfInlinePorDefecto() throws Exception {
        when(manualService.descargarManual("clientes")).thenReturn(descarga());

        mockMvc.perform(get("/api/v1/manuales/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("inline")));
    }

    @Test
    @WithMockUser
    void descargarManual_conDescargarTrue_devuelveAttachmentConElTituloVisible() throws Exception {
        when(manualService.descargarManual("clientes")).thenReturn(descarga());

        mockMvc.perform(get("/api/v1/manuales/clientes").param("descargar", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("Manual%20de%20M%C3%B3dulo%20Clientes.pdf")));
    }

    @Test
    @WithMockUser
    void descargarManual_conClaveInexistente_retornaNotFound() throws Exception {
        when(manualService.descargarManual("no-existe"))
                .thenThrow(new ManualNoEncontradoException("no-existe"));

        mockMvc.perform(get("/api/v1/manuales/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("MANUAL_NO_ENCONTRADO"));
    }

    @Test
    @WithMockUser
    void descargarManual_sinPdfPublicado_retornaNotFoundConCodigoPropio() throws Exception {
        when(manualService.descargarManual("finanzas"))
                .thenThrow(new ManualNoDisponibleException(Manual.FINANZAS.getTitulo()));

        mockMvc.perform(get("/api/v1/manuales/finanzas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("MANUAL_NO_DISPONIBLE"));
    }

    @Test
    void descargarManual_sinAutenticacion_retornaUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/manuales/clientes"))
                .andExpect(status().isUnauthorized());
    }
}
