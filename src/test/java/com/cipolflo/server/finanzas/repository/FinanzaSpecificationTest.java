package com.cipolflo.server.finanzas.repository;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class FinanzaSpecificationTest {

    @Autowired
    private FinanzaRepository finanzaRepository;

    private Ingreso ingresoReserva;
    private Egreso egresoUte;

    @BeforeEach
    void setUp() {
        finanzaRepository.deleteAll();

        ingresoReserva = Ingreso.crearManual(
                LocalDate.of(2026, 6, 15),
                BigDecimal.valueOf(1500),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Ingreso reserva"
        );

        egresoUte = Egreso.crearManual(
                LocalDate.of(2026, 6, 20),
                BigDecimal.valueOf(2000),
                Concepto.UTE,
                FormaPago.TRANSFERENCIA,
                Procedencia.CAMPING,
                "Egreso UTE"
        );

        ingresoReserva = (Ingreso) finanzaRepository.save(ingresoReserva);
        egresoUte = (Egreso) finanzaRepository.save(egresoUte);
    }

    @Test
    void deberiaFiltrarPorFechaDesdeInclusive() {
        List<Finanza> resultado = finanzaRepository.findAll(
                FinanzaSpecification.conFechaDesde(LocalDate.of(2026, 6, 20))
        );

        assertEquals(1, resultado.size());
        assertEquals(egresoUte.getId(), resultado.get(0).getId());
    }

    @Test
    void deberiaFiltrarPorFechaHastaInclusive() {
        List<Finanza> resultado = finanzaRepository.findAll(
                FinanzaSpecification.conFechaHasta(LocalDate.of(2026, 6, 15))
        );

        assertEquals(1, resultado.size());
        assertEquals(ingresoReserva.getId(), resultado.get(0).getId());
    }

    @Test
    void deberiaFiltrarPorConcepto() {
        List<Finanza> resultado = finanzaRepository.findAll(
                FinanzaSpecification.conConcepto(Concepto.UTE)
        );

        assertEquals(1, resultado.size());
        assertEquals(egresoUte.getId(), resultado.get(0).getId());
    }

    @Test
    void deberiaFiltrarPorTipoMovimientoIngreso() {
        List<Finanza> resultado = finanzaRepository.findAll(
                FinanzaSpecification.conTipoMovimiento(TipoMovimiento.INGRESO)
        );

        assertEquals(1, resultado.size());
        assertEquals(ingresoReserva.getId(), resultado.get(0).getId());
    }

    @Test
    void deberiaFiltrarPorTipoMovimientoEgreso() {
        List<Finanza> resultado = finanzaRepository.findAll(
                FinanzaSpecification.conTipoMovimiento(TipoMovimiento.EGRESO)
        );

        assertEquals(1, resultado.size());
        assertEquals(egresoUte.getId(), resultado.get(0).getId());
    }

    @Test
    void deberiaFiltrarCombinandoFiltros() {
        List<Finanza> resultado = finanzaRepository.findAll(
                FinanzaSpecification.desdeFiltros(
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 6, 30),
                        Concepto.UTE,
                        TipoMovimiento.EGRESO
                )
        );

        assertEquals(1, resultado.size());
        assertEquals(egresoUte.getId(), resultado.get(0).getId());
    }

    @Test
    void deberiaDevolverVacioCuandoFechaDesdeEsMayorQueFechaHasta() {
        List<Finanza> resultado = finanzaRepository.findAll(
                FinanzaSpecification.desdeFiltros(
                        LocalDate.of(2026, 6, 30),
                        LocalDate.of(2026, 6, 1),
                        null,
                        null
                )
        );

        assertEquals(0, resultado.size());
    }
}
