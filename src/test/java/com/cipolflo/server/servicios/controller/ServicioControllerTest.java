package com.cipolflo.server.servicios.controller;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.service.IServicioService;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicioController.class)
public class ServicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IServicioService servicioService;

    @Test
    @WithMockUser
    void deberiaLanzarErrorCuandoElIdEsInvalido() throws Exception {
        mockMvc.perform(get("/servicios/0"))
                .andExpect(status().isBadRequest());

        verify(servicioService, never()).getDetalleServicio(anyLong());
    }

    @Test
    void deberiaRetornarUnauthorizedCuandoUsuarioNoEstaLogueado() throws Exception {
        mockMvc.perform(get("/servicios/1"))
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

        mockMvc.perform(get("/servicios/1"))
                .andExpect(status().isOk());

        verify(servicioService).getDetalleServicio(servicioId);
    }

}