package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IClienteService clienteService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private PageResponse<ListadoClientesResponseDto> paginaVacia() {
        return new PageResponse<>(List.of(), 0, 10, 0, 0, true, true);
    }

    private ClienteResponseDto detalleCliente() {
        return new ClienteResponseDto(
                1L, "Juan Pérez", "12345678", LocalDate.of(1990, 1, 1),
                "099111111", "juan@mail.com", MetodoCobro.EFECTIVO,
                "Uruguay", "Montevideo", "Montevideo", "Av. 18 de Julio 100",
                5, TipoCliente.SOCIO, EstadoSocio.ACTIVO, null,
                null, null, null, null
        );
    }

    private PageResponse<ListadoClientesResponseDto> paginaConResultados() {
        ListadoClientesResponseDto dto = new ListadoClientesResponseDto(
                1L, "Juan Pérez", "12345678", "juan@mail.com", TipoCliente.SOCIO, 1, EstadoSocio.ACTIVO
        );
        return new PageResponse<>(List.of(dto), 0, 10, 1, 1, true, true);
    }

    @Test
    @WithMockUser
    void deberiaRetornarListadoSinFiltros() throws Exception {
        when(clienteService.getListadoClientes(any(ListadoClientesRequestDto.class), any(PageRequestDto.class)))
                .thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/clientes").param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(clienteService).getListadoClientes(any(ListadoClientesRequestDto.class), any(PageRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaFiltrarPorTipoClienteSocio() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/clientes")
                        .param("tipoCliente", "SOCIO")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(clienteService).getListadoClientes(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaFiltrarPorTipoClienteParticular() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/clientes")
                        .param("tipoCliente", "PARTICULAR")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deberiaFiltrarPorNombre() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/clientes")
                        .param("nombre", "juan")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(clienteService).getListadoClientes(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaFiltrarPorIdentificador() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/clientes")
                        .param("identificador", "1.234.567-8")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(clienteService).getListadoClientes(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaFiltrarPorEstado() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/clientes")
                        .param("estado", "ACTIVO")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(clienteService).getListadoClientes(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaFiltrarCombinandoVariosParametros() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaConResultados());

        mockMvc.perform(get("/api/v1/clientes")
                        .param("tipoCliente", "SOCIO")
                        .param("nombre", "juan")
                        .param("identificador", "12345678")
                        .param("estado", "ACTIVO")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(clienteService).getListadoClientes(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarListadoVacioCuandoNoHayCoincidencias() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/clientes")
                        .param("nombre", "nombreQueNoExiste")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoNoEstaAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/clientes").param("page", "0").param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoTipoClienteEsInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/clientes")
                        .param("tipoCliente", "INVALIDO")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoEstadoEsInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/clientes")
                        .param("estado", "INVALIDO")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoNombreSuperaLimite() throws Exception {
        mockMvc.perform(get("/api/v1/clientes")
                        .param("nombre", "a".repeat(101))
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoSizeEsCero() throws Exception {
        mockMvc.perform(get("/api/v1/clientes").param("page", "0").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoPageEsNegativo() throws Exception {
        mockMvc.perform(get("/api/v1/clientes").param("page", "-1").param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoSizeSuperaElMaximo() throws Exception {
        mockMvc.perform(get("/api/v1/clientes").param("page", "0").param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaUsarPaginacionPorDefectoCuandoNoSeEnvianParametros() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk());

        verify(clienteService).getListadoClientes(any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarDetalleDelClienteCuandoExiste() throws Exception {
        when(clienteService.getDetalleCliente(1L)).thenReturn(detalleCliente());

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk());

        verify(clienteService).getDetalleCliente(1L);
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoClienteNoExiste() throws Exception {
        when(clienteService.getDetalleCliente(99L)).thenThrow(new ClienteNotFoundException(99L));

        mockMvc.perform(get("/api/v1/clientes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deberiaRetornarUnauthorizedAlPedirDetalleSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoIdEsNegativoEnDetalle() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoIdEsTextoEnDetalle() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/abc"))
                .andExpect(status().isBadRequest());
    }

    // --- modificarParticular ---

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoIdEsNegativoEnModificarParticular() throws Exception {
        mockMvc.perform(put("/api/v1/clientes/particulares/-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"Juan\",\"telefono\":\"099000000\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltanCamposRequeridosEnModificarParticular() throws Exception {
        mockMvc.perform(put("/api/v1/clientes/particulares/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoParticularNoExisteOEsSocio() throws Exception {
        when(clienteService.modificarParticular(eq(99L), any())).thenThrow(new ClienteNotFoundException(99L));

        mockMvc.perform(put("/api/v1/clientes/particulares/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"Juan\",\"telefono\":\"099000000\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deberiaModificarParticularExitosamente() throws Exception {
        when(clienteService.modificarParticular(eq(1L), any())).thenReturn(detalleCliente());

        mockMvc.perform(put("/api/v1/clientes/particulares/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"Juan Pérez\",\"telefono\":\"099111111\"}"))
                .andExpect(status().isOk());

        verify(clienteService).modificarParticular(eq(1L), any());
    }

    // --- modificarSocio ---

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoIdEsNegativoEnModificarSocio() throws Exception {
        mockMvc.perform(put("/api/v1/clientes/socios/-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoSocio()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltanCamposRequeridosEnModificarSocio() throws Exception {
        mockMvc.perform(put("/api/v1/clientes/socios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoSocioNoExisteOEsParticular() throws Exception {
        when(clienteService.modificarSocio(eq(99L), any())).thenThrow(new ClienteNotFoundException(99L));

        mockMvc.perform(put("/api/v1/clientes/socios/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoSocio()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deberiaModificarSocioExitosamente() throws Exception {
        when(clienteService.modificarSocio(eq(1L), any())).thenReturn(detalleCliente());

        mockMvc.perform(put("/api/v1/clientes/socios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoSocio()))
                .andExpect(status().isOk());

        verify(clienteService).modificarSocio(eq(1L), any());
    }

    private String bodyValidoSocio() {
        return """
                {
                  "cedula": "12345678",
                  "nombreCompleto": "Juan Pérez",
                  "telefono": "099111111",
                  "fechaNacimiento": "1990-01-01",
                  "pais": "Uruguay",
                  "departamento": "Montevideo",
                  "ciudad": "Montevideo",
                  "direccion": "Av. 18 de Julio 100",
                  "metodoCobro": "EFECTIVO"
                }
                """;
    }
}
