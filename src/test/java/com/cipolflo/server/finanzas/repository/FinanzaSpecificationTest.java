package com.cipolflo.server.finanzas.repository;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinanzaSpecificationTest {

    @Test
    void deberiaCrearSpecificationConFechaDesde() {
        Specification<Finanza> spec = FinanzaSpecification.conFechaDesde(
                LocalDate.of(2026, 6, 1)
        );

        assertNotNull(spec);
    }

    @Test
    void deberiaCrearSpecificationConFechaHasta() {
        Specification<Finanza> spec = FinanzaSpecification.conFechaHasta(
                LocalDate.of(2026, 6, 30)
        );

        assertNotNull(spec);
    }

    @Test
    void deberiaCrearSpecificationConConcepto() {
        Specification<Finanza> spec = FinanzaSpecification.conConcepto(
                Concepto.PAGO_RESERVA
        );

        assertNotNull(spec);
    }

    @Test
    void deberiaCrearSpecificationConTipoMovimientoIngreso() {
        Specification<Finanza> spec = FinanzaSpecification.conTipoMovimiento(
                TipoMovimiento.INGRESO
        );

        assertNotNull(spec);
    }

    @Test
    void deberiaCrearSpecificationConTipoMovimientoEgreso() {
        Specification<Finanza> spec = FinanzaSpecification.conTipoMovimiento(
                TipoMovimiento.EGRESO
        );

        assertNotNull(spec);
    }

    @Test
    void deberiaCrearSpecificationCombinadaDesdeFiltros() {
        Specification<Finanza> spec = FinanzaSpecification.desdeFiltros(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                Concepto.PAGO_RESERVA,
                TipoMovimiento.INGRESO
        );

        assertNotNull(spec);
    }

    @Test
    void deberiaCrearSpecificationConFiltrosNulos() {
        Specification<Finanza> spec = FinanzaSpecification.desdeFiltros(
                null,
                null,
                null,
                null
        );

        assertNotNull(spec);
    }
}