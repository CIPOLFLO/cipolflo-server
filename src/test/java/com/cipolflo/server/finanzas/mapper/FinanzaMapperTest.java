package com.cipolflo.server.finanzas.mapper;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.FinanzaDetalleResponseDto;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinanzaMapperTest {

    @Test
    void deberiaMapearDetalleDeIngreso() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, 6, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Alta manual"
        );

        ingreso.setId(1L);

        FinanzaDetalleResponseDto dto = FinanzaMapper.toDetalleResponseDto(ingreso);

        assertEquals(1L, dto.getId());
        assertEquals(TipoMovimiento.INGRESO, dto.getTipoMovimiento());
        assertEquals(Procedencia.SEDE, dto.getProcedencia());
        assertEquals(Concepto.PAGO_RESERVA, dto.getConcepto());
        assertEquals(LocalDate.of(2026, 6, 15), dto.getFecha());
        assertEquals(BigDecimal.valueOf(1500), dto.getImporte());
        assertEquals(FormaPago.EFECTIVO, dto.getFormaPago());
        assertEquals("Alta manual", dto.getNotas());
    }

    @Test
    void deberiaMapearDetalleDeEgreso() {
        Egreso egreso = Egreso.crearManual(
                LocalDate.of(2026, 6, 15),
                BigDecimal.valueOf(2000),
                Concepto.UTE,
                FormaPago.TRANSFERENCIA,
                Procedencia.CAMPING,
                "Pago UTE"
        );

        egreso.setId(2L);

        FinanzaDetalleResponseDto dto = FinanzaMapper.toDetalleResponseDto(egreso);

        assertEquals(2L, dto.getId());
        assertEquals(TipoMovimiento.EGRESO, dto.getTipoMovimiento());
        assertEquals(Procedencia.CAMPING, dto.getProcedencia());
        assertEquals(Concepto.UTE, dto.getConcepto());
        assertEquals(LocalDate.of(2026, 6, 15), dto.getFecha());
        assertEquals(BigDecimal.valueOf(2000), dto.getImporte());
        assertEquals(FormaPago.TRANSFERENCIA, dto.getFormaPago());
        assertEquals("Pago UTE", dto.getNotas());
    }
}