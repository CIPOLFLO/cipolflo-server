package com.cipolflo.server.servicios.controller;

import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.servicios.dto.ModificacionServicioDto;
import com.cipolflo.server.servicios.dto.ServicioRegistroRequestDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioReservaOcupacionDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.servicios.exception.TarifaServicioNotFoundException;
import com.cipolflo.server.servicios.service.IServicioService;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import com.cipolflo.server.shared.pagination.PageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(
        controllers = ServicioController.class,
        excludeAutoConfiguration = {
                HibernateJpaAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = true)
public class ServicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockitoBean
    private IServicioService servicioService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private PageResponse<ListadoServiciosResponseDto> paginaVaciaServicios() {
        return new PageResponse<>(List.of(), 0, 10, 0, 0, true, true);
    }

    @Test
    @WithMockUser
    void deberiaLanzarErrorCuandoElIdEsInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/servicios/0"))
                .andExpect(status().isBadRequest());

        verify(servicioService, never()).getDetalleServicio(anyLong());
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaLogueado() throws Exception {
        mockMvc.perform(get("/api/v1/servicios/1"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser
    void deberiaRetornarOkCuandoUsuarioEstaLogueadoYElIdEsValido() throws Exception {

        Long servicioId = 1L;

        ServicioResponseDto response = new ServicioResponseDto(
                servicioId,
                "Cabaña",
                Procedencia.CAMPING,
                2,
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2500),
                4,
                null,
                EstadoServicio.HABILITADO,
                ModalidadPrecio.POR_DIA,
                List.of(),
                null, null, null, null
        );

        when(servicioService.getDetalleServicio(servicioId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/servicios/1"))
                .andExpect(status().isOk());

        verify(servicioService).getDetalleServicio(servicioId);
    }
    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoElServicioNoExiste() throws Exception {
        Long servicioId = 99L;

        when(servicioService.getDetalleServicio(servicioId))
                .thenThrow(new ServicioNotFoundException(servicioId));

        mockMvc.perform(get("/api/v1/servicios/99"))
                .andExpect(status().isNotFound());

        verify(servicioService).getDetalleServicio(servicioId);
    }


    @Test
    @WithMockUser
    void deberiaRetornarOkCuandoCambiaHabilitacionServicio() throws Exception {
        Long servicioId = 1L;

        ServicioResponseDto servicioResponse = new ServicioResponseDto(
                servicioId,
                "Cabaña",
                Procedencia.CAMPING,
                2,
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2500),
                4,
                null,
                EstadoServicio.DESHABILITADO,
                ModalidadPrecio.POR_DIA,
                List.of(),
                null, null, null, null
        );

        when(servicioService.cambiarHabilitacionServicio(
                eq(servicioId),
                any(ServicioRequestDto.class)
        )).thenReturn(servicioResponse);

        mockMvc.perform(
                        patch("/api/v1/servicios/1/habilitacion")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "habilitado": false
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        verify(servicioService).cambiarHabilitacionServicio(
                eq(servicioId),
                any(ServicioRequestDto.class)
        );
    }
    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoElIdEsInvalidoAlCambiarHabilitacion() throws Exception {
        mockMvc.perform(patch("/api/v1/servicios/0/habilitacion").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "habilitado": false
                            }
                            """)
        ).andExpect(status().isBadRequest());


        verify(servicioService, never()).cambiarHabilitacionServicio(anyLong(), any(ServicioRequestDto.class));
    }
    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaLogueadoAlCambiarHabilitacion() throws Exception {
        mockMvc.perform(patch("/api/v1/servicios/1/habilitacion").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaCampoHabilitado() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/servicios/1/habilitacion")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verify(servicioService, never()).cambiarHabilitacionServicio(
                anyLong(),
                any(ServicioRequestDto.class)
        );
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaLogueadoAlModificarServicio() throws Exception {
        mockMvc.perform(put("/api/v1/servicios/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoElIdEsInvalidoAlModificarServicio() throws Exception {
        mockMvc.perform(put("/api/v1/servicios/0")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA",
                            "capacidad": 4,
                            "cantidad": 2
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).modificarServicio(anyLong(), any(ModificacionServicioDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltanCamposObligatoriosAlModificarServicio() throws Exception {
        mockMvc.perform(put("/api/v1/servicios/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {}
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).modificarServicio(anyLong(), any(ModificacionServicioDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarOkCuandoModificaServicioExitosamente() throws Exception {
        Long servicioId = 1L;

        ServicioResponseDto response = new ServicioResponseDto(
                servicioId,
                "Cabaña Premium",
                Procedencia.CAMPING,
                2,
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                4,
                null,
                EstadoServicio.HABILITADO,
                ModalidadPrecio.POR_DIA,
                List.of(),
                null, null, null, null
        );

        when(servicioService.modificarServicio(eq(servicioId), any(ModificacionServicioDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/servicios/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "precioParticular": 3000,
                            "precioSocio": 2000,
                            "modalidadPrecio": "POR_DIA",
                            "capacidad": 4,
                            "cantidad": 2,
                            "tarifas": [
                                {
                                    "tipoCliente": "PARTICULAR",
                                    "precio": 3000,
                                    "modalidadPrecio": "POR_DIA"
                                },
                                {
                                    "tipoCliente": "SOCIO_COMUN",
                                    "precio": 2000,
                                    "modalidadPrecio": "POR_DIA"
                                }
                            ]
                        }
                        """)
        ).andExpect(status().isOk());

        verify(servicioService).modificarServicio(eq(servicioId), any(ModificacionServicioDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoElServicioNoExisteAlModificar() throws Exception {
        Long servicioId = 99L;

        when(servicioService.modificarServicio(eq(servicioId), any(ModificacionServicioDto.class)))
                .thenThrow(new ServicioNotFoundException(servicioId));

        mockMvc.perform(put("/api/v1/servicios/99")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA",
                            "capacidad": 4,
                            "cantidad": 2,
                            "tarifas": [
                                {
                                    "tipoCliente": "PARTICULAR",
                                    "precio": 3000,
                                    "modalidadPrecio": "POR_DIA"
                                },
                                {
                                    "tipoCliente": "SOCIO_COMUN",
                                    "precio": 2000,
                                    "modalidadPrecio": "POR_DIA"
                                }
                            ]
                        }
                        """)
        ).andExpect(status().isNotFound());

        verify(servicioService).modificarServicio(eq(servicioId), any(ModificacionServicioDto.class));
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaLogueadoAlRegistrarServicio() throws Exception {
        mockMvc.perform(post("/api/v1/servicios").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deberiaRetornarCreatedCuandoRegistraServicioExitosamente() throws Exception {
        ServicioResponseDto response = new ServicioResponseDto(
                1L,
                "Cabaña Premium",
                Procedencia.CAMPING,
                2,
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2500),
                4,
                null,
                EstadoServicio.HABILITADO,
                ModalidadPrecio.POR_DIA,
                List.of(),
                null, null, null, null
        );

        when(servicioService.registrarServicio(any(ServicioRegistroRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA",
                            "capacidad": 4,
                            "cantidad": 2,
                            "tarifas": [
                                {
                                  "tipoCliente": "PARTICULAR",
                                  "precio": 2500,
                                  "modalidadPrecio": "POR_DIA"
                                },
                                {
                                  "tipoCliente": "SOCIO_COMUN",
                                  "precio": 1500,
                                  "modalidadPrecio": "POR_DIA"
                                }
                            ]
                        }
                        """)
        ).andExpect(status().isCreated());

        verify(servicioService).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaNombreAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA"
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoNombreEstaVacioAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "",
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA"
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaProcedenciaAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA"
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaPrecioParticularAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA"
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaPrecioSocioAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "modalidadPrecio": "POR_DIA"
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaModalidadPrecioAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "precioSocio": 1500
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoPrecioParticularEsCeroAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioParticular": 0,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA"
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoPrecioParticularEsNegativoAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioParticular": -100,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA"
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCapacidadEsCeroAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA",
                            "capacidad": 0
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoCantidadEsNegativaAlRegistrar() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA",
                            "cantidad": -1
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService, never()).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoNombreYaExisteAlRegistrar() throws Exception {
        when(servicioService.registrarServicio(any(ServicioRegistroRequestDto.class)))
                .thenThrow(new ServicioValidacionException(
                        ServicioCodigoError.NOMBRE_DUPLICADO.name(),
                        "Ya existe un servicio con ese nombre"
                ));

        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Existente",
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA",
                            "tarifas": [
                              {
                                "tipoCliente": "PARTICULAR",
                                "precio": 2500,
                                "modalidadPrecio": "POR_DIA"
                              },
                              {
                                "tipoCliente": "SOCIO_COMUN",
                                "precio": 1500,
                                "modalidadPrecio": "POR_DIA"
                              }
                            ]
                        }
                        """)
        ).andExpect(status().isBadRequest());

        verify(servicioService).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaAceptarSortFieldValidoCuandoSeListanServicios() throws Exception {
        when(servicioService.getListadoServicios(any(), any())).thenReturn(paginaVaciaServicios());

        mockMvc.perform(get("/api/v1/servicios")
                        .param("page", "0").param("size", "10")
                        .param("sortField", "nombre")
                        .param("sortOrder", "ASC"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoSortFieldEsInvalidoCuandoSeListanServicios() throws Exception {
        mockMvc.perform(get("/api/v1/servicios")
                        .param("page", "0").param("size", "10")
                        .param("sortField", "campoInexistente"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoSortOrderEsInvalidoCuandoSeListanServicios() throws Exception {
        mockMvc.perform(get("/api/v1/servicios")
                        .param("page", "0").param("size", "10")
                        .param("sortField", "nombre")
                        .param("sortOrder", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaPermitirCapacidadYCantidadNulosAlRegistrar() throws Exception {
        ServicioResponseDto response = new ServicioResponseDto(
                1L,
                "Cabaña Premium",
                Procedencia.CAMPING,
                null,
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2500),
                null,
                null,
                EstadoServicio.HABILITADO,
                ModalidadPrecio.POR_DIA,
                List.of(),
                null, null, null, null
        );

        when(servicioService.registrarServicio(any(ServicioRegistroRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/servicios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Cabaña Premium",
                            "procedencia": "CAMPING",
                            "precioParticular": 2500,
                            "precioSocio": 1500,
                            "modalidadPrecio": "POR_DIA",
                            "tarifas": [
                              {
                                "tipoCliente": "PARTICULAR",
                                "precio": 2500,
                                "modalidadPrecio": "POR_DIA"
                              },
                              {
                                "tipoCliente": "SOCIO_COMUN",
                                "precio": 1500,
                                "modalidadPrecio": "POR_DIA"
                              }
                            ]
                        }
                        """)
        ).andExpect(status().isCreated());

        verify(servicioService).registrarServicio(any(ServicioRegistroRequestDto.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarOkConFechasOcupadas() throws Exception {
        when(servicioService.getFechasOcupadas(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new ServicioReservaOcupacionDto(
                        1L,
                        EstadoReserva.CONFIRMADA,
                        LocalDate.of(2026, 6, 17),
                        LocalDate.of(2026, 6, 19)
                )));

        mockMvc.perform(get("/api/v1/servicios/1/fechas-ocupadas")
                        .param("desde", "2026-06-16")
                        .param("hasta", "2026-06-20"))
                .andExpect(status().isOk());

        verify(servicioService).getFechasOcupadas(eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoElIdEsInvalidoEnFechasOcupadas() throws Exception {
        mockMvc.perform(get("/api/v1/servicios/0/fechas-ocupadas")
                        .param("desde", "2026-06-16")
                        .param("hasta", "2026-06-20"))
                .andExpect(status().isBadRequest());

        verify(servicioService, never()).getFechasOcupadas(anyLong(), any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoFaltaParametroDeFechaEnFechasOcupadas() throws Exception {
        mockMvc.perform(get("/api/v1/servicios/1/fechas-ocupadas")
                        .param("desde", "2026-06-16"))
                .andExpect(status().isBadRequest());

        verify(servicioService, never()).getFechasOcupadas(anyLong(), any(), any());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoElServicioNoExisteEnFechasOcupadas() throws Exception {
        when(servicioService.getFechasOcupadas(eq(99L), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new ServicioNotFoundException(99L));

        mockMvc.perform(get("/api/v1/servicios/99/fechas-ocupadas")
                        .param("desde", "2026-06-16")
                        .param("hasta", "2026-06-20"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaLogueadoEnFechasOcupadas() throws Exception {
        mockMvc.perform(get("/api/v1/servicios/1/fechas-ocupadas")
                        .param("desde", "2026-06-16")
                        .param("hasta", "2026-06-20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNoContentCuandoEliminaTarifaExitosamente() throws Exception {
        doNothing().when(servicioService).eliminarTarifaDeServicio(1L, 10L);

        mockMvc.perform(delete("/api/v1/servicios/1/tarifas/10").with(csrf()))
                .andExpect(status().isNoContent());

        verify(servicioService).eliminarTarifaDeServicio(1L, 10L);
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoElServicioNoExisteAlEliminarTarifa() throws Exception {
        doThrow(new ServicioNotFoundException(99L))
                .when(servicioService).eliminarTarifaDeServicio(99L, 10L);

        mockMvc.perform(delete("/api/v1/servicios/99/tarifas/10").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deberiaRetornarNotFoundCuandoLaTarifaNoExisteOPerteneceAOtroServicio() throws Exception {
        doThrow(new TarifaServicioNotFoundException(10L, 1L))
                .when(servicioService).eliminarTarifaDeServicio(1L, 10L);

        mockMvc.perform(delete("/api/v1/servicios/1/tarifas/10").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoLaTarifaEsLaUltimaObligatoria() throws Exception {
        doThrow(new ServicioValidacionException(
                ServicioCodigoError.TARIFA_OBLIGATORIA_NO_ELIMINABLE.name(),
                "No es posible eliminar la única tarifa de tipo PARTICULAR del servicio"
        )).when(servicioService).eliminarTarifaDeServicio(1L, 10L);

        mockMvc.perform(delete("/api/v1/servicios/1/tarifas/10").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoElIdDelServicioEsInvalidoAlEliminarTarifa() throws Exception {
        mockMvc.perform(delete("/api/v1/servicios/0/tarifas/10").with(csrf()))
                .andExpect(status().isBadRequest());

        verify(servicioService, never()).eliminarTarifaDeServicio(anyLong(), anyLong());
    }

    @Test
    @WithMockUser
    void deberiaRetornarBadRequestCuandoElIdDeLaTarifaEsInvalidoAlEliminarTarifa() throws Exception {
        mockMvc.perform(delete("/api/v1/servicios/1/tarifas/0").with(csrf()))
                .andExpect(status().isBadRequest());

        verify(servicioService, never()).eliminarTarifaDeServicio(anyLong(), anyLong());
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaLogueadoAlEliminarTarifa() throws Exception {
        mockMvc.perform(delete("/api/v1/servicios/1/tarifas/10").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
