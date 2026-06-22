package com.cipolflo.server.finanzas.controller;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.*;
import com.cipolflo.server.finanzas.exception.FinanzaNotFoundException;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinanzaController.class)
class FinanzaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IFinanzaService finanzaService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void deberiaRegistrarIngresoValido() throws Exception {
        when(finanzaService.registrarFinanza(any(FinanzaCrearRequestDto.class)))
                .thenReturn(response(TipoMovimiento.INGRESO));

        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "fecha": "2026-06-15",
                                  "importe": 1500,
                                  "formaPago": "EFECTIVO",
                                  "notas": "Alta manual"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoMovimiento").value("INGRESO"))
                .andExpect(jsonPath("$.procedencia").value("SEDE"))
                .andExpect(jsonPath("$.concepto").value("PAGO_RESERVA"))
                .andExpect(jsonPath("$.importe").value(1500))
                .andExpect(jsonPath("$.formaPago").value("EFECTIVO"))
                .andExpect(jsonPath("$.reservaId").value(nullValue()))
                .andExpect(jsonPath("$.pagoCuotaId").value(nullValue()));

        verify(finanzaService).registrarFinanza(any(FinanzaCrearRequestDto.class));
    }

    @Test
    void deberiaRegistrarEgresoValido() throws Exception {
        when(finanzaService.registrarFinanza(any(FinanzaCrearRequestDto.class)))
                .thenReturn(response(TipoMovimiento.EGRESO));

        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "EGRESO",
                                  "procedencia": "CAMPING",
                                  "concepto": "UTE",
                                  "fecha": "2026-06-15",
                                  "importe": 2000,
                                  "formaPago": "TRANSFERENCIA",
                                  "notas": "Pago UTE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("EGRESO"));

        verify(finanzaService).registrarFinanza(any(FinanzaCrearRequestDto.class));
    }

    @Test
    void deberiaRetornarUnauthorizedSinUsuarioAutenticado() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": 1500,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoFaltaTipoMovimiento() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": 1500,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoFaltaProcedencia() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": 1500,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoFaltaConcepto() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "importe": 1500,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoFaltaFormaPago() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": 1500
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoImporteEsCero() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": 0,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoImporteEsNegativo() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": -100,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoConceptoNoExiste() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "CONCEPTO_INVALIDO",
                                  "importe": 1500,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRegistrarCuandoFechaNoSeEnvia() throws Exception {
        when(finanzaService.registrarFinanza(any(FinanzaCrearRequestDto.class)))
                .thenReturn(response(TipoMovimiento.INGRESO));

        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": 1500,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(finanzaService).registrarFinanza(any(FinanzaCrearRequestDto.class));
    }

    @Test
    void deberiaRetornarBadRequestCuandoFaltaImporte() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoImporteTieneMasDeDosDecimales() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "INGRESO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": 1500.999,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoTipoMovimientoNoExiste() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoMovimiento": "GASTO",
                                  "procedencia": "SEDE",
                                  "concepto": "PAGO_RESERVA",
                                  "importe": 1500,
                                  "formaPago": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    @Test
    void deberiaRetornarBadRequestCuandoJsonEstaMalformado() throws Exception {
        mockMvc.perform(post("/api/v1/finanzas")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"tipoMovimiento\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));

        verify(finanzaService, never()).registrarFinanza(any());
    }

    private FinanzaResponseDto response(TipoMovimiento tipoMovimiento) {
        return new FinanzaResponseDto(
                1L,
                tipoMovimiento,
                tipoMovimiento == TipoMovimiento.INGRESO ? Procedencia.SEDE : Procedencia.CAMPING,
                tipoMovimiento == TipoMovimiento.INGRESO ? Concepto.PAGO_RESERVA : Concepto.UTE,
                LocalDate.of(2026, 6, 15),
                BigDecimal.valueOf(1500),
                tipoMovimiento == TipoMovimiento.INGRESO ? FormaPago.EFECTIVO : FormaPago.TRANSFERENCIA,
                "Alta manual",
                null,
                null
        );    }
    @Test
    void deberiaRetornarDetalleFinanza() throws Exception {
        when(finanzaService.getDetalleFinanza(1L))
                .thenReturn(detalleResponse());

        mockMvc.perform(get("/api/v1/finanzas/1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoMovimiento").value("INGRESO"))
                .andExpect(jsonPath("$.procedencia").value("SEDE"));
    }
    @Test
    void deberiaRetornar404CuandoNoExisteFinanza() throws Exception {

        when(finanzaService.getDetalleFinanza(99L))
                .thenThrow(new FinanzaNotFoundException(99L));

        mockMvc.perform(get("/api/v1/finanzas/99")
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo")
                        .value("FINANZA_NO_ENCONTRADA"));
    }
    @Test
    void deberiaRetornar400CuandoIdEsNegativo() throws Exception {

        mockMvc.perform(get("/api/v1/finanzas/-1")
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }
    @Test
    void deberiaRetornar401SinAutenticacion() throws Exception {

        mockMvc.perform(get("/api/v1/finanzas/1"))
                .andExpect(status().isUnauthorized());
    }

    private FinanzaDetalleResponseDto detalleResponse() {
        return new FinanzaDetalleResponseDto(
                1L,
                TipoMovimiento.INGRESO,
                Procedencia.SEDE,
                Concepto.PAGO_RESERVA,
                LocalDate.of(2026, 6, 15),
                BigDecimal.valueOf(1500),
                FormaPago.EFECTIVO,
                "Alta manual",
                Instant.now(),
                Instant.now(),
                "admin",
                "admin"
        );
    }
    @Test
    void deberiaExportarFinanzas() throws Exception {
        when(finanzaService.exportarFinanzas(any(FinanzaExportRequestDto.class)))
                .thenReturn(new ArchivoExportado(
                        "finanzas_2026-06-22_1427.xlsx",
                        new byte[]{1, 2, 3}
                ));

        mockMvc.perform(post("/api/v1/finanzas/export")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("finanzas_2026-06-22_1427.xlsx")
                ))
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));

        verify(finanzaService).exportarFinanzas(any(FinanzaExportRequestDto.class));
    }
    @Test
    void deberiaExportarFinanzasConFiltros() throws Exception {
        when(finanzaService.exportarFinanzas(any(FinanzaExportRequestDto.class)))
                .thenReturn(new ArchivoExportado(
                        "finanzas_2026-06-22_1427.xlsx",
                        new byte[]{1, 2, 3}
                ));

        mockMvc.perform(post("/api/v1/finanzas/export")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "fechaDesde": "2026-06-01",
                              "fechaHasta": "2026-06-30",
                              "concepto": "PAGO_RESERVA",
                              "tipoMovimiento": "INGRESO"
                            }
                            """))
                .andExpect(status().isOk());

        verify(finanzaService).exportarFinanzas(any(FinanzaExportRequestDto.class));
    }
    @Test
    void deberiaRetornar401AlExportarSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/finanzas/exportar"))
                .andExpect(status().isUnauthorized());

        verify(finanzaService, never()).exportarFinanzas(any());
    }

    @Test
    void deberiaRetornarListadoFinanzas() throws Exception {
        when(finanzaService.getListadoFinanzas(
                any(ListadoFinanzasRequestDto.class),
                any(PageRequestDto.class)
        )).thenReturn(listadoResponse());

        mockMvc.perform(get("/api/v1/finanzas")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].concepto").value("PAGO_RESERVA"))
                .andExpect(jsonPath("$.content[0].tipoMovimiento").value("INGRESO"));

        verify(finanzaService).getListadoFinanzas(
                any(ListadoFinanzasRequestDto.class),
                any(PageRequestDto.class)
        );
    }

    @Test
    void deberiaRetornarListadoFinanzasConFiltros() throws Exception {
        when(finanzaService.getListadoFinanzas(
                any(ListadoFinanzasRequestDto.class),
                any(PageRequestDto.class)
        )).thenReturn(listadoResponse());

        mockMvc.perform(get("/api/v1/finanzas")
                        .param("fechaDesde", "2026-06-01")
                        .param("fechaHasta", "2026-06-30")
                        .param("concepto", "PAGO_RESERVA")
                        .param("tipoMovimiento", "INGRESO")
                        .with(jwt()))
                .andExpect(status().isOk());

        verify(finanzaService).getListadoFinanzas(
                any(ListadoFinanzasRequestDto.class),
                any(PageRequestDto.class)
        );
    }

    @Test
    void deberiaRetornarBadRequestCuandoSortFieldEsInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/finanzas")
                        .param("sortField", "concepto")
                        .with(jwt()))
                .andExpect(status().isBadRequest());

        verify(finanzaService, never()).getListadoFinanzas(any(), any());
    }

    @Test
    void deberiaRetornarUnauthorizedAlListarSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/finanzas"))
                .andExpect(status().isUnauthorized());

        verify(finanzaService, never()).getListadoFinanzas(any(), any());
    }
    private PageResponse<ListadoFinanzasResponseDto> listadoResponse() {
        return new PageResponse<>(
                List.of(new ListadoFinanzasResponseDto(
                        1L,
                        Concepto.PAGO_RESERVA,
                        LocalDate.of(2026, 6, 15),
                        BigDecimal.valueOf(1500),
                        "Alta manual",
                        TipoMovimiento.INGRESO
                )),
                0,
                10,
                1,
                1,
                true,
                true
        );
    }


}