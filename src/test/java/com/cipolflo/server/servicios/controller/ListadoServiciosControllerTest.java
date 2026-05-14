package com.cipolflo.server.servicios.controller;

import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ListadoServiciosRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.servicios.service.IServicioService;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicioController.class)
public class ListadoServiciosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IServicioService servicioService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private PageResponse<ListadoServiciosResponseDto> paginaVacia() {
        return new PageResponse<>(List.of(), 0, 10, 0, 0, true, true);
    }

    private PageResponse<ListadoServiciosResponseDto> paginaConResultados() {
        ListadoServiciosResponseDto dto = new ListadoServiciosResponseDto(
                1L, "Cabaña", Procedencia.CAMPING,
                BigDecimal.valueOf(2500), BigDecimal.valueOf(1500),
                ModalidadPrecio.POR_DIA, EstadoServicio.HABILITADO
        );
        return new PageResponse<>(List.of(dto), 0, 10, 1, 1, true, true);
    }

    @Test
    @WithMockUser
    void deberiaRetornarListadoCompletoSinFiltros() throws Exception {
        when(servicioService.getListadoServicios(any(ListadoServiciosRequestDto.class), any(PageRequestDto.class)))
                .thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/servicios").param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(servicioService).getListadoServicios(any(ListadoServiciosRequestDto.class), any(PageRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaFiltrarPorProcedencia() throws Exception {
        when(servicioService.getListadoServicios(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/servicios")
                        .param("procedencia", "CAMPING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(servicioService).getListadoServicios(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaFiltrarPorNombre() throws Exception {
        when(servicioService.getListadoServicios(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/servicios")
                        .param("nombre", "caba")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(servicioService).getListadoServicios(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaFiltrarPorEstado() throws Exception {
        when(servicioService.getListadoServicios(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/servicios")
                        .param("estado", "HABILITADO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(servicioService).getListadoServicios(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaFiltrarCombinandoVariosParametros() throws Exception {
        when(servicioService.getListadoServicios(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/servicios")
                        .param("procedencia", "CAMPING")
                        .param("nombre", "caba")
                        .param("estado", "HABILITADO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(servicioService).getListadoServicios(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarListadoVacioCuandoNoHayCoincidencias() throws Exception {
        when(servicioService.getListadoServicios(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/servicios")
                        .param("nombre", "nombreQueNoExiste")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/servicios").param("page", "0").param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoProcedenciaEsInvalida() throws Exception {
        mockMvc.perform(get("/api/v1/servicios")
                        .param("procedencia", "INVALIDO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoEstadoEsInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/servicios")
                        .param("estado", "INVALIDO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaUsarPaginacionPorDefectoCuandoNoSeEnvianParametros() throws Exception {
        when(servicioService.getListadoServicios(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/servicios"))
                .andExpect(status().isOk());

        verify(servicioService).getListadoServicios(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaIgnorarParametroDesconocido() throws Exception {
        when(servicioService.getListadoServicios(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/servicios")
                        .param("parametroRaro", "valorRaro")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
