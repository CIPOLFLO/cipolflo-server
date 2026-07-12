package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.*;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.clientes.service.IRegistroParticularService;
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
import java.time.Month;
import java.util.List;
import java.util.Optional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IClienteService clienteService;

    @MockitoBean
    private IRegistroParticularService registroParticularService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private PageResponse<ListadoClientesResponseDto> paginaVacia() {
        return new PageResponse<>(List.of(), 0, 10, 0, 0, true, true);
    }

    private ClienteResponseDto detalleCliente() {
        return new ClienteResponseDto(
                1L, "Juan Pérez", "12345678", null, LocalDate.of(1990, Month.JANUARY, 1),
                "099111111", "juan@mail.com", MetodoCobro.EFECTIVO,
                "Uruguay", "Montevideo", "Montevideo", "Av. 18 de Julio 100",
                5, TipoCliente.SOCIO, EstadoSocio.ACTIVO, null,
                null, null, null, null, null
        );
    }

    private PageResponse<ListadoClientesResponseDto> paginaConResultados() {
        ListadoClientesResponseDto dto = new ListadoClientesResponseDto(
                1L,
                "Juan Pérez",
                "12345678",
                null,
                "juan@mail.com",
                TipoCliente.SOCIO,
                1,
                EstadoSocio.ACTIVO,
                null
        );
        return new PageResponse<>(List.of(dto), 0, 10, 1, 1, true, true);
    }

    private BusquedaCedulaResponseDto busquedaParticular() {
        return new BusquedaCedulaResponseDto(
                1L, "Laura Fernández", "12345678",
                "099000000", "laura@mail.com", null,
                TipoCliente.PARTICULAR
        );
    }

    private BusquedaCedulaResponseDto busquedaSocio() {
        return new BusquedaCedulaResponseDto(
                2L, "Juan Pérez", "12345678",
                "099111111", "juan@mail.com", null,
                TipoCliente.SOCIO
        );
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

    @Test
    @WithMockUser
    void deberiaAceptarSortFieldValidoCuandoSeListanClientes() throws Exception {
        when(clienteService.getListadoClientes(any(), any())).thenReturn(paginaVacia());

        mockMvc.perform(get("/api/v1/clientes")
                        .param("page", "0").param("size", "10")
                        .param("sortField", "nombreCompleto")
                        .param("sortOrder", "ASC"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoSortFieldEsInvalidoCuandoSeListanClientes() throws Exception {
        mockMvc.perform(get("/api/v1/clientes")
                        .param("page", "0").param("size", "10")
                        .param("sortField", "campoInexistente"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoSortOrderEsInvalidoCuandoSeListanClientes() throws Exception {
        mockMvc.perform(get("/api/v1/clientes")
                        .param("page", "0").param("size", "10")
                        .param("sortField", "nombreCompleto")
                        .param("sortOrder", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaDarDeBajaSocioExitosamente() throws Exception {
        Long socioId = 1L;

        mockMvc.perform(
                        patch("/api/v1/clientes/socios/1/baja")
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(clienteService).darDeBajaSocio(socioId);
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoIdEsInvalido() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/clientes/socios/0/baja")
                                .with(csrf())
                )
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).darDeBajaSocio(anyLong());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoSocioNoExiste() throws Exception {
        Long socioId = 99L;

        doThrow(new SocioNotFoundException(socioId))
                .when(clienteService).darDeBajaSocio(socioId);

        mockMvc.perform(
                        patch("/api/v1/clientes/socios/99/baja")
                                .with(csrf())
                )
                .andExpect(status().isNotFound());

        verify(clienteService).darDeBajaSocio(socioId);
    }

    // --- consultarEstadoSocio ---

    private EstadoSocioResponseDto estadoSocio() {
        return new EstadoSocioResponseDto(1L, EstadoSocio.ACTIVO, 5, 0);
    }

    @Test
    @WithMockUser
    void deberiaRetornarEstadoDelSocioCuandoExiste() throws Exception {
        when(clienteService.consultarEstadoSocio(1L)).thenReturn(estadoSocio());

        mockMvc.perform(get("/api/v1/clientes/socios/1/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.numeroSocio").value(5))
                .andExpect(jsonPath("$.mesesSinPagar").value(0));

        verify(clienteService).consultarEstadoSocio(1L);
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoSocioNoExisteEnConsultaEstado() throws Exception {
        when(clienteService.consultarEstadoSocio(99L)).thenThrow(new SocioNotFoundException(99L));

        mockMvc.perform(get("/api/v1/clientes/socios/99/estado"))
                .andExpect(status().isNotFound());

        verify(clienteService).consultarEstadoSocio(99L);
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoIdEsDeParticularEnConsultaEstado() throws Exception {
        when(clienteService.consultarEstadoSocio(2L)).thenThrow(new SocioNotFoundException(2L));

        mockMvc.perform(get("/api/v1/clientes/socios/2/estado"))
                .andExpect(status().isNotFound());

        verify(clienteService).consultarEstadoSocio(2L);
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoIdNoEsPositivoEnConsultaEstado() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/socios/0/estado"))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).consultarEstadoSocio(anyLong());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoIdNoEsNumericoEnConsultaEstado() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/socios/abc/estado"))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).consultarEstadoSocio(anyLong());
    }

    @Test
    void deberiaRetornarUnauthorizedAlConsultarEstadoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/socios/1/estado"))
                .andExpect(status().isUnauthorized());
    }

    // --- modificarParticular ---

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoIdEsNegativoEnModificarParticular() throws Exception {
        mockMvc.perform(put("/api/v1/clientes/particulares/-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoParticular()))
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
                        .content(bodyValidoParticular()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deberiaModificarParticularExitosamente() throws Exception {
        when(clienteService.modificarParticular(eq(1L), any())).thenReturn(detalleCliente());

        mockMvc.perform(put("/api/v1/clientes/particulares/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoParticular()))
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

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCedulaEsInvalidaEnModificarParticular() throws Exception {
        when(clienteService.modificarParticular(eq(1L), any()))
                .thenThrow(new ClienteValidacionException("CEDULA_INVALIDA", "La cédula ingresada no es válida"));

        mockMvc.perform(put("/api/v1/clientes/particulares/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoParticular()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCedulaEsDuplicadaEnModificarParticular() throws Exception {
        when(clienteService.modificarParticular(eq(1L), any()))
                .thenThrow(new ClienteValidacionException("CEDULA_DUPLICADA", "Ya existe un cliente con esa cédula"));

        mockMvc.perform(put("/api/v1/clientes/particulares/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoParticular()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoEmailEsInvalidoEnModificarParticular() throws Exception {
        when(clienteService.modificarParticular(eq(1L), any()))
                .thenThrow(new ClienteValidacionException("EMAIL_INVALIDO", "El email ingresado no es válido"));

        mockMvc.perform(put("/api/v1/clientes/particulares/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cedula\":\"12345672\",\"nombreCompleto\":\"Juan\",\"telefono\":\"099000000\",\"mail\":\"no-es-un-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoEmailEsDuplicadoEnModificarParticular() throws Exception {
        when(clienteService.modificarParticular(eq(1L), any()))
                .thenThrow(new ClienteValidacionException("EMAIL_DUPLICADO", "El email ingresado ya está en uso"));

        mockMvc.perform(put("/api/v1/clientes/particulares/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cedula\":\"12345672\",\"nombreCompleto\":\"Juan\",\"telefono\":\"099000000\",\"mail\":\"juan@mail.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoEmailEsInvalidoEnModificarSocio() throws Exception {
        when(clienteService.modificarSocio(eq(1L), any()))
                .thenThrow(new ClienteValidacionException("EMAIL_INVALIDO", "El email ingresado no es válido"));

        mockMvc.perform(put("/api/v1/clientes/socios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoSocioConEmail("no-es-un-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoEmailEsDuplicadoEnModificarSocio() throws Exception {
        when(clienteService.modificarSocio(eq(1L), any()))
                .thenThrow(new ClienteValidacionException("EMAIL_DUPLICADO", "El email ingresado ya está en uso"));

        mockMvc.perform(put("/api/v1/clientes/socios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoSocioConEmail("juan@mail.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCedulaEsInvalidaEnModificarSocio() throws Exception {
        when(clienteService.modificarSocio(eq(1L), any()))
                .thenThrow(new ClienteValidacionException("CEDULA_INVALIDA", "La cédula ingresada no es válida"));

        mockMvc.perform(put("/api/v1/clientes/socios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoSocio()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCedulaEsDuplicadaEnModificarSocio() throws Exception {
        when(clienteService.modificarSocio(eq(1L), any()))
                .thenThrow(new ClienteValidacionException("CEDULA_DUPLICADA", "Ya existe un cliente con esa cédula"));

        mockMvc.perform(put("/api/v1/clientes/socios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValidoSocio()))
                .andExpect(status().isBadRequest());
    }

    // --- buscarPorCedula ---

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoCedulaNoEstaRegistrada() throws Exception {
        when(clienteService.buscarPorCedula("99999999")).thenThrow(new ClienteNotFoundException("Cliente no encontrado"));

        mockMvc.perform(get("/api/v1/clientes/cedula/99999999"))
                .andExpect(status().isNotFound());

        verify(clienteService).buscarPorCedula("99999999");
    }

    @Test
    @WithMockUser
    void deberiaRetornarParticularCuandoCedulaCorrespondeAParticular() throws Exception {
        when(clienteService.buscarPorCedula("12345678")).thenReturn(busquedaParticular());

        mockMvc.perform(get("/api/v1/clientes/cedula/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCliente").value("PARTICULAR"));

        verify(clienteService).buscarPorCedula("12345678");
    }

    @Test
    @WithMockUser
    void deberiaRetornarSocioCuandoCedulaCorrespondeASocio() throws Exception {
        when(clienteService.buscarPorCedula("12345678")).thenReturn((busquedaSocio()));

        mockMvc.perform(get("/api/v1/clientes/cedula/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCliente").value("SOCIO"));

        verify(clienteService).buscarPorCedula("12345678");
    }

    @Test
@WithMockUser
void deberiaRetornarBadRequestCuandoFormatoDeCedulaEsInvalido() throws Exception {
    doThrow(new ClienteValidacionException(
            ClienteCodigoError.CEDULA_INVALIDA.name(),
            "La cédula ingresada no es válida"
    )).when(clienteService).buscarPorCedula("formato-invalido");

    mockMvc.perform(get("/api/v1/clientes/cedula/formato-invalido"))
            .andExpect(status().isBadRequest());

    verify(clienteService).buscarPorCedula("formato-invalido");
}

    @Test
    @WithMockUser
    void deberiaEncontrarMismoRegistroConCedulaFormateadaYSinFormatear() throws Exception {
        when(clienteService.buscarPorCedula(any())).thenReturn((busquedaParticular()));

        mockMvc.perform(get("/api/v1/clientes/cedula/12345678"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/clientes/cedula/1.234.567-8"))
                .andExpect(status().isOk());

        verify(clienteService, times(2)).buscarPorCedula(any());
    }

    @Test
    void deberiaRetornarUnauthorizedAlBuscarPorCedulaSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/cedula/12345678"))
                .andExpect(status().isUnauthorized());
    }

    // --- registrarSocio ---

    @Test
    @WithMockUser
    void deberiaRegistrarSocioCorrectamente() throws Exception {
        ClienteResponseDto response = new ClienteResponseDto(
                1L, "Juan Pérez", "12345678", null,
                LocalDate.of(1990, Month.MAY, 10),
                "099123456", "juan@mail.com", MetodoCobro.EFECTIVO,
                "Uruguay", "Montevideo", "Montevideo", "Av. Italia 1234",
                7, TipoCliente.SOCIO, EstadoSocio.ACTIVO, "Sin observaciones",
                null, null, null, null, null
        );

        when(clienteService.registrarSocio(any(RegistroSocioRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/clientes/socios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "cedula": "1.234.567-8",
                                      "nombreCompleto": "Juan Pérez",
                                      "fechaNacimiento": "1990-05-10",
                                      "telefono": "099123456",
                                      "email": "juan@mail.com",
                                      "metodoCobro": "EFECTIVO",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Av. Italia 1234",
                                      "observaciones": "Sin observaciones"
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCliente").value("SOCIO"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        verify(clienteService).registrarSocio(any(RegistroSocioRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaCampoRequerido() throws Exception {
        mockMvc.perform(
                        post("/api/v1/clientes/socios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "cedula": "",
                                      "nombreCompleto": "Juan Pérez",
                                      "fechaNacimiento": "1990-05-10",
                                      "telefono": "099123456",
                                      "metodoCobro": "EFECTIVO",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Av. Italia 1234"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(clienteService, never()).registrarSocio(any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCedulaDuplicada() throws Exception {
        when(clienteService.registrarSocio(any(RegistroSocioRequestDto.class)))
                .thenThrow(new ClienteValidacionException(
                        ClienteCodigoError.CEDULA_DUPLICADA.name(),
                        "Ya existe un cliente con esa cédula"
                ));

        mockMvc.perform(
                        post("/api/v1/clientes/socios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "cedula": "1.234.567-8",
                                      "nombreCompleto": "Juan Pérez",
                                      "fechaNacimiento": "1990-05-10",
                                      "telefono": "099123456",
                                      "metodoCobro": "EFECTIVO",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Av. Italia 1234"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CEDULA_DUPLICADA"));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoMetodoCobroEsInvalido() throws Exception {
        mockMvc.perform(
                        post("/api/v1/clientes/socios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "cedula": "1.234.567-8",
                                      "nombreCompleto": "Juan Pérez",
                                      "fechaNacimiento": "1990-05-10",
                                      "telefono": "099123456",
                                      "metodoCobro": "INVALIDO",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Av. Italia 1234"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(clienteService, never()).registrarSocio(any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCedulaEsInvalida() throws Exception {
        when(clienteService.registrarSocio(any(RegistroSocioRequestDto.class)))
                .thenThrow(new ClienteValidacionException(
                        "SOLICITUD_INVALIDA",
                        "La cédula ingresada no es válida"
                ));

        mockMvc.perform(
                        post("/api/v1/clientes/socios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "cedula": "123",
                                      "nombreCompleto": "Juan Pérez",
                                      "fechaNacimiento": "1990-05-10",
                                      "telefono": "099123456",
                                      "metodoCobro": "EFECTIVO",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Av. Italia 1234"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    @WithMockUser
    void deberiaRegistrarParticularCorrectamente() throws Exception {
        ClienteResponseDto response = new ClienteResponseDto(
                1L,
                "Juan Pérez",
                "12345678",
                null,
                null,
                "099123456",
                "juan@mail.com",
                null,
                null,
                null,
                null,
                null,
                null,
                TipoCliente.PARTICULAR,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(registroParticularService.registrarParticular(any(RegistroParticularRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/clientes/particulares")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "cedula": "1.234.567-8",
                                  "nombre": "Juan Pérez",
                                  "celular": "099123456",
                                  "mail": "juan@mail.com"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCliente").value("PARTICULAR"))
                .andExpect(jsonPath("$.cedula").value("12345678"));

        verify(registroParticularService).registrarParticular(any(RegistroParticularRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaCampoRequeridoEnRegistroParticular() throws Exception {
        mockMvc.perform(
                        post("/api/v1/clientes/particulares")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "cedula": "",
                                  "nombre": "Juan Pérez",
                                  "celular": "099123456"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(registroParticularService, never()).registrarParticular(any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCedulaDuplicadaEnRegistroParticular() throws Exception {
        when(registroParticularService.registrarParticular(any(RegistroParticularRequestDto.class)))
                .thenThrow(new ClienteValidacionException(
                        ClienteCodigoError.CEDULA_DUPLICADA.name(),
                        "Ya existe un cliente con esa cédula"
                ));

        mockMvc.perform(
                        post("/api/v1/clientes/particulares")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "cedula": "1.234.567-8",
                                  "nombre": "Juan Pérez",
                                  "celular": "099123456"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CEDULA_DUPLICADA"));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCedulaEsInvalidaEnRegistroParticular() throws Exception {
        when(registroParticularService.registrarParticular(any(RegistroParticularRequestDto.class)))
                .thenThrow(new ClienteValidacionException(
                        ClienteCodigoError.CEDULA_INVALIDA.name(),
                        "La cédula ingresada no es válida"
                ));

        mockMvc.perform(
                        post("/api/v1/clientes/particulares")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "cedula": "abc",
                                  "nombre": "Juan Pérez",
                                  "celular": "099123456"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CEDULA_INVALIDA"));
    }

    private String bodyValidoParticular() {
        return "{\"cedula\":\"12345672\",\"nombreCompleto\":\"Juan Pérez\",\"telefono\":\"099111111\"}";
    }

    private String bodyValidoSocio() {
        return bodyValidoSocioConEmail(null);
    }

    private String bodyValidoSocioConEmail(String mail) {
        String mailJson = mail != null ? "\"mail\": \"" + mail + "\"," : "";
        return """
                {
                  "cedula": "12345672",
                  "nombreCompleto": "Juan Pérez",
                  "telefono": "099111111",
                  %s
                  "fechaNacimiento": "1990-01-01",
                  "pais": "Uruguay",
                  "departamento": "Montevideo",
                  "ciudad": "Montevideo",
                  "direccion": "Av. 18 de Julio 100",
                  "metodoCobro": "EFECTIVO"
                }
                """.formatted(mailJson);
    }
       // --- exportarClientes ---

    @Test
    @WithMockUser
    void deberiaExportarClientesCorrectamente() throws Exception {
        com.cipolflo.server.shared.export.ArchivoExportado archivo = new com.cipolflo.server.shared.export.ArchivoExportado(
                "clientes.xlsx",
                "excel".getBytes()
        );

        when(clienteService.exportarClientes(any())).thenReturn(archivo);

        mockMvc.perform(post("/api/v1/clientes/exportar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": null}"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"clientes.xlsx\""
                ));
    }

    @Test
    @WithMockUser
    void deberiaExportarAunqueNoHayaClientes() throws Exception {
        com.cipolflo.server.shared.export.ArchivoExportado archivo = new com.cipolflo.server.shared.export.ArchivoExportado(
                "clientes.xlsx",
                new byte[0]
        );

        when(clienteService.exportarClientes(any())).thenReturn(archivo);

        mockMvc.perform(post("/api/v1/clientes/exportar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().exists(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @WithMockUser
    void deberiaLlamarAlService() throws Exception {
        when(clienteService.exportarClientes(any()))
                .thenReturn(new com.cipolflo.server.shared.export.ArchivoExportado("clientes.xlsx", new byte[0]));

        mockMvc.perform(post("/api/v1/clientes/exportar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(clienteService).exportarClientes(any());
    }

    @Test
    @WithMockUser
    void deberiaDevolverErrorSiFallaElService() throws Exception {
        when(clienteService.exportarClientes(any()))
                .thenThrow(new RuntimeException("Error exportando"));

        mockMvc.perform(post("/api/v1/clientes/exportar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is5xxServerError());
    }

    // --- registrarEmpresa ---

    @Test
    @WithMockUser
    void deberiaRegistrarEmpresaCorrectamente() throws Exception {
        ClienteResponseDto response = new ClienteResponseDto(
                1L, "Antel S.A.", null, "211003420017",
                null,
                "099123456", "empresa@mail.com", null,
                "Uruguay", "Montevideo", "Montevideo", "Guatemala 1075",
                null, TipoCliente.EMPRESA, null, "Sin observaciones",
                null, null, null, null, null
        );

        when(clienteService.registrarEmpresa(any(RegistroEmpresaRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/clientes/empresas")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "razonSocial": "Antel S.A.",
                                      "rut": "21.100342.001-7",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Guatemala 1075",
                                      "telefono": "099123456",
                                      "mail": "empresa@mail.com",
                                      "observaciones": "Sin observaciones"
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCliente").value("EMPRESA"))
                .andExpect(jsonPath("$.rut").value("211003420017"));

        verify(clienteService).registrarEmpresa(any(RegistroEmpresaRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaCampoRequeridoEnRegistroEmpresa() throws Exception {
        mockMvc.perform(
                        post("/api/v1/clientes/empresas")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "razonSocial": "",
                                      "rut": "21.100342.001-7",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Guatemala 1075",
                                      "telefono": "099123456"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(clienteService, never()).registrarEmpresa(any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoRutEsInvalidoEnRegistroEmpresa() throws Exception {
        when(clienteService.registrarEmpresa(any(RegistroEmpresaRequestDto.class)))
                .thenThrow(new ClienteValidacionException(
                        ClienteCodigoError.RUT_INVALIDO.name(),
                        "El RUT ingresado no es válido"
                ));

        mockMvc.perform(
                        post("/api/v1/clientes/empresas")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "razonSocial": "Antel S.A.",
                                      "rut": "123",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Guatemala 1075",
                                      "telefono": "099123456"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("RUT_INVALIDO"));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoRutDuplicadoEnRegistroEmpresa() throws Exception {
        when(clienteService.registrarEmpresa(any(RegistroEmpresaRequestDto.class)))
                .thenThrow(new ClienteValidacionException(
                        ClienteCodigoError.RUT_DUPLICADO.name(),
                        "Ya existe un cliente con ese RUT"
                ));

        mockMvc.perform(
                        post("/api/v1/clientes/empresas")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "razonSocial": "Antel S.A.",
                                      "rut": "21.100342.001-7",
                                      "pais": "Uruguay",
                                      "departamento": "Montevideo",
                                      "ciudad": "Montevideo",
                                      "direccion": "Guatemala 1075",
                                      "telefono": "099123456"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("RUT_DUPLICADO"));
    }

    // --- buscarPorRut ---

    private BusquedaRutResponseDto busquedaEmpresa() {
        return new BusquedaRutResponseDto(
                1L, "Antel S.A.", "211003420017",
                "099123456", "empresa@mail.com", null,
                TipoCliente.EMPRESA
        );
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoRutNoEstaRegistrado() throws Exception {
        when(clienteService.buscarPorRut("999999999999"))
                .thenThrow(new ClienteNotFoundException("Cliente no encontrado"));

        mockMvc.perform(get("/api/v1/clientes/rut/999999999999"))
                .andExpect(status().isNotFound());

        verify(clienteService).buscarPorRut("999999999999");
    }

    @Test
    @WithMockUser
    void deberiaRetornarEmpresaCuandoRutCorrespondeAEmpresa() throws Exception {
        when(clienteService.buscarPorRut("211003420017")).thenReturn(busquedaEmpresa());

        mockMvc.perform(get("/api/v1/clientes/rut/211003420017"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCliente").value("EMPRESA"))
                .andExpect(jsonPath("$.rut").value("211003420017"));

        verify(clienteService).buscarPorRut("211003420017");
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFormatoDeRutEsInvalido() throws Exception {
        doThrow(new ClienteValidacionException(
                ClienteCodigoError.RUT_INVALIDO.name(),
                "El RUT ingresado no es válido"
        )).when(clienteService).buscarPorRut("formato-invalido");

        mockMvc.perform(get("/api/v1/clientes/rut/formato-invalido"))
                .andExpect(status().isBadRequest());

        verify(clienteService).buscarPorRut("formato-invalido");
    }

    @Test
    @WithMockUser
    void deberiaEncontrarMismoRegistroConRutFormateadoYSinFormatear() throws Exception {
        when(clienteService.buscarPorRut(any())).thenReturn(busquedaEmpresa());

        mockMvc.perform(get("/api/v1/clientes/rut/211003420017"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/clientes/rut/21.100342.001-7"))
                .andExpect(status().isOk());

        verify(clienteService, times(2)).buscarPorRut(any());
    }

    @Test
    void deberiaRetornarUnauthorizedAlBuscarPorRutSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/rut/211003420017"))
                .andExpect(status().isUnauthorized());
    }
}
