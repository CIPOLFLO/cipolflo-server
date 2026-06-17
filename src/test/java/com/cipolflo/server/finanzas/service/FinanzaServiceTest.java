package com.cipolflo.server.finanzas.service;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.dto.FinanzaResponseDto;
import com.cipolflo.server.finanzas.repository.FinanzaRepository;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanzaServiceTest {

    @Mock
    private FinanzaRepository finanzaRepository;

    @InjectMocks
    private FinanzaService finanzaService;

    @Test
    void deberiaRegistrarIngresoManualCorrectamente() {
        FinanzaCrearRequestDto dto = crearDto(TipoMovimiento.INGRESO);
        dto.setFecha(LocalDate.of(2026, 6, 15));

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
        assertEquals(LocalDate.of(2026, 6, 15), response.getFecha());
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
        dto.setFecha(LocalDate.of(2026, 1, 20));

        when(finanzaRepository.save(any(Finanza.class)))
                .thenAnswer(invocation -> {
                    Egreso egreso = invocation.getArgument(0);
                    egreso.setId(1L);
                    return egreso;
                });

        FinanzaResponseDto response = finanzaService.registrarFinanza(dto);

        assertEquals(LocalDate.of(2026, 1, 20), response.getFecha());
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
        dto.setFecha(LocalDate.of(2026, 6, 15));
        dto.setImporte(BigDecimal.valueOf(1500));
        dto.setFormaPago(FormaPago.EFECTIVO);
        dto.setNotas("Alta manual");
        return dto;
    }
}