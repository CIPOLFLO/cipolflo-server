package com.cipolflo.server.finanzas.service;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.*;
import com.cipolflo.server.finanzas.exception.FinanzaNotFoundException;
import com.cipolflo.server.finanzas.repository.FinanzaRepository;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.export.ExportProperties;
import com.cipolflo.server.shared.export.ExportacionException;
import com.cipolflo.server.shared.export.IExportService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanzaServiceTest {

    @Mock
    private FinanzaRepository finanzaRepository;
    @Mock
    private IExportService exportService;
    @Mock
    private ExportProperties exportProperties;
    @InjectMocks
    private FinanzaService finanzaService;

    @Test
    void deberiaRegistrarIngresoManualCorrectamente() {
        FinanzaCrearRequestDto dto = crearDto(TipoMovimiento.INGRESO);
        dto.setFecha(LocalDate.of(2026, Month.JUNE, 15));

        when(finanzaRepository.save(any(Finanza.class)))
                .thenAnswer(invocation -> {
                    Ingreso ingreso = invocation.getArgument(0);
                    ingreso.setId(1L);
                    return ingreso;
                });

        FinanzaResponseDto response = finanzaService.registrarFinanza(dto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(TipoMovimiento.INGRESO, response.getTipoMovimiento());
        assertEquals(Procedencia.SEDE, response.getProcedencia());
        assertEquals(Concepto.PAGO_RESERVA, response.getConcepto());
        assertEquals(LocalDate.of(2026, Month.JUNE, 15), response.getFecha());
        assertEquals(BigDecimal.valueOf(1500), response.getImporte());
        assertEquals(FormaPago.EFECTIVO, response.getFormaPago());
        assertEquals("Alta manual", response.getNotas());
        assertNull(response.getReservaId());
        assertNull(response.getPagoCuotaId());

        ArgumentCaptor<Finanza> captor = ArgumentCaptor.forClass(Finanza.class);
        verify(finanzaRepository).save(captor.capture());
        assertInstanceOf(Ingreso.class, captor.getValue());
    }

    @Test
    void deberiaRegistrarEgresoManualCorrectamente() {
        FinanzaCrearRequestDto dto = crearDto(TipoMovimiento.EGRESO);
        dto.setConcepto(Concepto.UTE);
        dto.setProcedencia(Procedencia.CAMPING);

        when(finanzaRepository.save(any(Finanza.class)))
                .thenAnswer(invocation -> {
                    Egreso egreso = invocation.getArgument(0);
                    egreso.setId(2L);
                    return egreso;
                });

        FinanzaResponseDto response = finanzaService.registrarFinanza(dto);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(TipoMovimiento.EGRESO, response.getTipoMovimiento());
        assertEquals(Procedencia.CAMPING, response.getProcedencia());
        assertEquals(Concepto.UTE, response.getConcepto());
        assertNull(response.getReservaId());
        assertNull(response.getPagoCuotaId());

        ArgumentCaptor<Finanza> captor = ArgumentCaptor.forClass(Finanza.class);
        verify(finanzaRepository).save(captor.capture());
        assertInstanceOf(Egreso.class, captor.getValue());
    }

    @Test
    void deberiaUsarFechaActualCuandoFechaEsNula() {
        FinanzaCrearRequestDto dto = crearDto(TipoMovimiento.INGRESO);
        dto.setFecha(null);

        when(finanzaRepository.save(any(Finanza.class)))
                .thenAnswer(invocation -> {
                    Ingreso ingreso = invocation.getArgument(0);
                    ingreso.setId(1L);
                    return ingreso;
                });

        LocalDate antes = LocalDate.now();

        FinanzaResponseDto response = finanzaService.registrarFinanza(dto);

        LocalDate despues = LocalDate.now();

        assertFalse(response.getFecha().isBefore(antes));
        assertFalse(response.getFecha().isAfter(despues));
    }

    @Test
    void deberiaRespetarFechaInformada() {
        FinanzaCrearRequestDto dto = crearDto(TipoMovimiento.EGRESO);
        dto.setFecha(LocalDate.of(2026, Month.JANUARY, 20));

        when(finanzaRepository.save(any(Finanza.class)))
                .thenAnswer(invocation -> {
                    Egreso egreso = invocation.getArgument(0);
                    egreso.setId(1L);
                    return egreso;
                });

        FinanzaResponseDto response = finanzaService.registrarFinanza(dto);

        assertEquals(LocalDate.of(2026, Month.JANUARY, 20), response.getFecha());
    }

    @Test
    void deberiaTrasladarImporteConceptoFormaPagoYNotas() {
        FinanzaCrearRequestDto dto = crearDto(TipoMovimiento.INGRESO);
        dto.setImporte(BigDecimal.valueOf(9999));
        dto.setConcepto(Concepto.BARRACA);
        dto.setFormaPago(FormaPago.TRANSFERENCIA);
        dto.setNotas("Compra de materiales");

        when(finanzaRepository.save(any(Finanza.class)))
                .thenAnswer(invocation -> {
                    Ingreso ingreso = invocation.getArgument(0);
                    ingreso.setId(1L);
                    return ingreso;
                });

        FinanzaResponseDto response = finanzaService.registrarFinanza(dto);

        assertEquals(BigDecimal.valueOf(9999), response.getImporte());
        assertEquals(Concepto.BARRACA, response.getConcepto());
        assertEquals(FormaPago.TRANSFERENCIA, response.getFormaPago());
        assertEquals("Compra de materiales", response.getNotas());
    }

    private FinanzaCrearRequestDto crearDto(TipoMovimiento tipoMovimiento) {
        FinanzaCrearRequestDto dto = new FinanzaCrearRequestDto();
        dto.setTipoMovimiento(tipoMovimiento);
        dto.setProcedencia(Procedencia.SEDE);
        dto.setConcepto(Concepto.PAGO_RESERVA);
        dto.setFecha(LocalDate.of(2026, Month.JUNE, 15));
        dto.setImporte(BigDecimal.valueOf(1500));
        dto.setFormaPago(FormaPago.EFECTIVO);
        dto.setNotas("Alta manual");
        return dto;
    }
    @Test
    void deberiaRetornarDetalleDeIngreso() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Alta manual"
        );

        ingreso.setId(1L);

        when(finanzaRepository.findById(1L))
                .thenReturn(Optional.of(ingreso));

        FinanzaDetalleResponseDto response =
                finanzaService.getDetalleFinanza(1L);

        assertEquals(1L, response.getId());
        assertEquals(TipoMovimiento.INGRESO, response.getTipoMovimiento());
        assertEquals(Procedencia.SEDE, response.getProcedencia());
        assertEquals(Concepto.PAGO_RESERVA, response.getConcepto());
        assertEquals(LocalDate.of(2026, Month.JUNE, 15), response.getFecha());
        assertEquals(BigDecimal.valueOf(1500), response.getImporte());
        assertEquals(FormaPago.EFECTIVO, response.getFormaPago());
        assertEquals("Alta manual", response.getNotas());
    }
    @Test
    void deberiaRetornarDetalleDeEgreso() {
        Egreso egreso = Egreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 15),
                BigDecimal.valueOf(2000),
                Concepto.UTE,
                FormaPago.TRANSFERENCIA,
                Procedencia.CAMPING,
                "Pago UTE"
        );

        egreso.setId(2L);

        when(finanzaRepository.findById(2L))
                .thenReturn(Optional.of(egreso));

        FinanzaDetalleResponseDto response =
                finanzaService.getDetalleFinanza(2L);

        assertEquals(2L, response.getId());
        assertEquals(TipoMovimiento.EGRESO, response.getTipoMovimiento());
        assertEquals(Procedencia.CAMPING, response.getProcedencia());
        assertEquals(Concepto.UTE, response.getConcepto());
        assertEquals(LocalDate.of(2026, Month.JUNE, 15), response.getFecha());
        assertEquals(BigDecimal.valueOf(2000), response.getImporte());
        assertEquals(FormaPago.TRANSFERENCIA, response.getFormaPago());
        assertEquals("Pago UTE", response.getNotas());
    }
    @Test
    void deberiaRetornarDetalleConNotasNulas() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                null
        );
        ingreso.setId(1L);

        when(finanzaRepository.findById(1L))
                .thenReturn(Optional.of(ingreso));

        FinanzaDetalleResponseDto response = finanzaService.getDetalleFinanza(1L);

        assertNull(response.getNotas());
    }

    @Test
    void deberiaMapearCamposDeAuditoriaEnDetalle() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Alta manual"
        );
        ingreso.setId(1L);

        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-06-15T10:00:00Z");
        ReflectionTestUtils.setField(ingreso, "createdAt", createdAt);
        ReflectionTestUtils.setField(ingreso, "updatedAt", updatedAt);
        ReflectionTestUtils.setField(ingreso, "createdBy", "admin");
        ReflectionTestUtils.setField(ingreso, "updatedBy", "editor");

        when(finanzaRepository.findById(1L))
                .thenReturn(Optional.of(ingreso));

        FinanzaDetalleResponseDto response = finanzaService.getDetalleFinanza(1L);

        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
        assertEquals("admin", response.getCreatedBy());
        assertEquals("editor", response.getUpdatedBy());
    }

    @Test
    void deberiaLanzarFinanzaNotFoundException() {
        when(finanzaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                FinanzaNotFoundException.class,
                () -> finanzaService.getDetalleFinanza(99L)
        );
    }
    @Test
    void deberiaExportarFinanzasSinFiltros() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Alta manual"
        );
        ingreso.setId(1L);

        when(finanzaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(ingreso));
        when(exportService.generarExcel(anyString(), anyList(), anyList(), any(int[].class)))
                .thenReturn(new byte[]{1, 2, 3});
        when(exportProperties.maxFilas()).thenReturn(50000);
        ArchivoExportado archivo = finanzaService.exportarFinanzas(new ListadoFinanzasRequestDto(null,null,null,null));

        assertNotNull(archivo);
        assertTrue(archivo.getNombre().startsWith("finanzas_"));
        assertTrue(archivo.getNombre().endsWith(".xlsx"));
        assertArrayEquals(new byte[]{1, 2, 3}, archivo.getContenido());

        verify(finanzaRepository).findAll(any(Specification.class));
        verify(exportService).generarExcel(eq("Finanzas"), anyList(), anyList(), any(int[].class));
    }
    @Test
    void deberiaExportarFilaDeIngresoConDatosCorrectos() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Alta manual"
        );

        when(finanzaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(ingreso));
        when(exportService.generarExcel(anyString(), anyList(), anyList(), any(int[].class)))
                .thenReturn(new byte[]{1});
        when(exportProperties.maxFilas()).thenReturn(50000);
        finanzaService.exportarFinanzas(new ListadoFinanzasRequestDto(null,null,null,null));

        ArgumentCaptor<List<List<String>>> filasCaptor = ArgumentCaptor.forClass(List.class);

        verify(exportService).generarExcel(
                eq("Finanzas"),
                anyList(),
                filasCaptor.capture(),
                any(int[].class)
        );

        List<String> fila = filasCaptor.getValue().get(0);

        assertEquals("INGRESO", fila.get(0));
        assertEquals("SEDE", fila.get(1));
        assertEquals("PAGO_RESERVA", fila.get(2));
        assertEquals("2026-06-15", fila.get(3));
        assertEquals("1500", fila.get(4));
        assertEquals("EFECTIVO", fila.get(5));
        assertEquals("Alta manual", fila.get(6));
    }
    @Test
    void deberiaExportarFilaDeEgresoConDatosCorrectos() {
        Egreso egreso = Egreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 20),
                BigDecimal.valueOf(2000),
                Concepto.UTE,
                FormaPago.TRANSFERENCIA,
                Procedencia.CAMPING,
                null
        );

        when(finanzaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(egreso));
        when(exportService.generarExcel(anyString(), anyList(), anyList(), any(int[].class)))
                .thenReturn(new byte[]{1});
        when(exportProperties.maxFilas()).thenReturn(50000);
        finanzaService.exportarFinanzas(new ListadoFinanzasRequestDto(null,null,null,null));

        ArgumentCaptor<List<List<String>>> filasCaptor = ArgumentCaptor.forClass(List.class);

        verify(exportService).generarExcel(
                eq("Finanzas"),
                anyList(),
                filasCaptor.capture(),
                any(int[].class)
        );

        List<String> fila = filasCaptor.getValue().get(0);

        assertEquals("EGRESO", fila.get(0));
        assertEquals("CAMPING", fila.get(1));
        assertEquals("UTE", fila.get(2));
        assertEquals("2026-06-20", fila.get(3));
        assertEquals("2000", fila.get(4));
        assertEquals("TRANSFERENCIA", fila.get(5));
        assertEquals("", fila.get(6));
    }

    @Test
    void deberiaRetornarListadoFinanzas() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Alta manual"
        );
        ingreso.setId(1L);

        when(finanzaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ingreso)));

        PageRequestDto pageRequest = new PageRequestDto(0, 10, null, null);
        ListadoFinanzasRequestDto filtros = new ListadoFinanzasRequestDto(
                null,
                null,
                null,
                null
        );

        PageResponse<ListadoFinanzasResponseDto> response =
                finanzaService.getListadoFinanzas(filtros, pageRequest);

        assertEquals(1, response.totalElements());
        assertEquals(1, response.content().size());
        assertEquals(1L, response.content().get(0).getId());
        assertEquals(Concepto.PAGO_RESERVA, response.content().get(0).getConcepto());
        assertEquals(TipoMovimiento.INGRESO, response.content().get(0).getTipoMovimiento());

        verify(finanzaRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaMapearTipoMovimientoIngresoYEgresoEnListado() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Ingreso"
        );
        ingreso.setId(1L);

        Egreso egreso = Egreso.crearManual(
                LocalDate.of(2026, Month.JUNE, 16),
                BigDecimal.valueOf(2000),
                Concepto.UTE,
                FormaPago.TRANSFERENCIA,
                Procedencia.CAMPING,
                "Egreso"
        );
        egreso.setId(2L);

        when(finanzaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ingreso, egreso)));

        PageRequestDto pageRequest = new PageRequestDto(0, 10, null, null);
        ListadoFinanzasRequestDto filtros = new ListadoFinanzasRequestDto(
                null,
                null,
                null,
                null
        );

        PageResponse<ListadoFinanzasResponseDto> response =
                finanzaService.getListadoFinanzas(filtros, pageRequest);

        assertEquals(TipoMovimiento.INGRESO, response.content().get(0).getTipoMovimiento());
        assertEquals(TipoMovimiento.EGRESO, response.content().get(1).getTipoMovimiento());
    }
@Test
void deberiaEliminarFinanzaExistente() {
    Ingreso ingreso = Ingreso.crearManual(
            LocalDate.of(2026, Month.JUNE, 15),
            BigDecimal.valueOf(1500),
            Concepto.PAGO_RESERVA,
            FormaPago.EFECTIVO,
            Procedencia.SEDE,
            "Alta manual"
    );
    ingreso.setId(1L);

    when(finanzaRepository.findById(1L))
            .thenReturn(Optional.of(ingreso));

    finanzaService.eliminarFinanza(1L);

    verify(finanzaRepository).findById(1L);
    verify(finanzaRepository).delete(ingreso);
}

@Test
void deberiaLanzarFinanzaNotFoundExceptionAlEliminar() {
    when(finanzaRepository.findById(99L))
            .thenReturn(Optional.empty());

    assertThrows(
            FinanzaNotFoundException.class,
            () -> finanzaService.eliminarFinanza(99L)
    );

    verify((CrudRepository<Finanza, Long>) finanzaRepository, never()).delete(any(Finanza.class));
}
    @Test
    void deberiaLanzarExportacionExceptionCuandoNoHayRegistrosParaExportar() {
        when(finanzaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        ExportacionException exception = assertThrows(
                ExportacionException.class,
                () -> finanzaService.exportarFinanzas(
                        new ListadoFinanzasRequestDto(null, null, null, null)
                )
        );

        assertEquals(
                "No hay registros que coincidan con los filtros aplicados",
                exception.getMessage()
        );

        verify(exportService, never()).generarExcel(anyString(), anyList(), anyList(), any(int[].class));
    }
    @Test
    void deberiaRegistrarPagoReserva() {
        FinanzaCrearRequestDto dto = new FinanzaCrearRequestDto();
        dto.setFecha(LocalDate.of(2026, 6, 28));
        dto.setImporte(BigDecimal.valueOf(1500));
        dto.setFormaPago(FormaPago.EFECTIVO);
        dto.setProcedencia(Procedencia.CAMPING);
        dto.setNotas("Pago de reserva");
        dto.setReservaId(10L);

        when(finanzaRepository.save(any(Finanza.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        finanzaService.registrarPagoReserva(dto);

        verify(finanzaRepository).save(argThat(finanza ->
                finanza instanceof Ingreso ingreso
                        && ingreso.getFecha().equals(LocalDate.of(2026, 6, 28))
                        && ingreso.getImporte().compareTo(BigDecimal.valueOf(1500)) == 0
                        && ingreso.getFormaPago().equals(FormaPago.EFECTIVO)
                        && ingreso.getProcedencia().equals(Procedencia.CAMPING)
                        && ingreso.getNotas().equals("Pago de reserva")
                        && ingreso.getReservaId().equals(10L)
                        && ingreso.getPagoCuotaId() == null
                        && ingreso.getConcepto().equals(Concepto.PAGO_RESERVA)
        ));
    }
    @Test
    void deberiaCrearIngresoDesdePagoCuota() {
        Ingreso ingreso = Ingreso.crearDesdePagoCuota(
                LocalDate.of(2026, 7, 4),
                BigDecimal.valueOf(1000),
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Pago cuota",
                15L
        );

        assertEquals(Concepto.PAGO_CUOTA, ingreso.getConcepto());
        assertEquals(BigDecimal.valueOf(1000), ingreso.getImporte());
        assertEquals(FormaPago.EFECTIVO, ingreso.getFormaPago());
        assertEquals(Procedencia.SEDE, ingreso.getProcedencia());
        assertEquals("Pago cuota", ingreso.getNotas());
        assertEquals(15L, ingreso.getPagoCuotaId());
        assertNull(ingreso.getReservaId());
    }

    @Test
    void deberiaRegistrarPagoCuotaComoIngreso() {
        FinanzaCrearRequestDto dto = new FinanzaCrearRequestDto();
        dto.setFecha(LocalDate.of(2026, 7, 4));
        dto.setImporte(BigDecimal.valueOf(1000));
        dto.setFormaPago(FormaPago.EFECTIVO);
        dto.setProcedencia(Procedencia.SEDE);
        dto.setNotas("Pago cuota");
        dto.setPagoCuotaId(15L);

        when(finanzaRepository.save(any(Finanza.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        finanzaService.registrarPagoCuota(dto);

        ArgumentCaptor<Finanza> captor = ArgumentCaptor.forClass(Finanza.class);
        verify(finanzaRepository).save(captor.capture());

        Ingreso ingreso = (Ingreso) captor.getValue();

        assertEquals(Concepto.PAGO_CUOTA, ingreso.getConcepto());
        assertEquals(BigDecimal.valueOf(1000), ingreso.getImporte());
        assertEquals(FormaPago.EFECTIVO, ingreso.getFormaPago());
        assertEquals(15L, ingreso.getPagoCuotaId());
        assertNull(ingreso.getReservaId());
    }



}