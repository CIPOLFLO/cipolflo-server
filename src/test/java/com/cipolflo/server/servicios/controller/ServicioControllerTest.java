package com.cipolflo.server.servicios.controller;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.service.IServicioService;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                true,
                ModalidadPrecio.POR_DIA
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
                false,
                ModalidadPrecio.POR_DIA
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
}
