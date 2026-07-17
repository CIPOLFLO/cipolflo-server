package com.cipolflo.server.finanzas.repository;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FinanzaRepositoryTest {

    @Autowired
    private FinanzaRepository finanzaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void deberiaPersistirYRecuperarUnIngresoManual() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.of(2026, 6, 15),
                new BigDecimal("1500.50"),
                Concepto.PAGO_RESERVA,
                FormaPago.EFECTIVO,
                Procedencia.SEDE,
                "Alta manual"
        );

        Long id = finanzaRepository.saveAndFlush(ingreso).getId();
        entityManager.clear();

        Finanza recuperada = finanzaRepository.findById(id).orElseThrow();

        assertInstanceOf(Ingreso.class, recuperada);
        assertEquals(LocalDate.of(2026, 6, 15), recuperada.getFecha());
        assertEquals(0, new BigDecimal("1500.50").compareTo(recuperada.getImporte()));
        assertEquals(Concepto.PAGO_RESERVA, recuperada.getConcepto());
        assertEquals(Procedencia.SEDE, recuperada.getProcedencia());
        assertEquals(FormaPago.EFECTIVO, recuperada.getFormaPago());
        assertEquals("Alta manual", recuperada.getNotas());

        Ingreso ingresoRecuperado = (Ingreso) recuperada;
        assertNull(ingresoRecuperado.getReservaId());
        assertNull(ingresoRecuperado.getPagoCuotaId());
    }

    @Test
    void deberiaPersistirYRecuperarUnEgresoManual() {
        Egreso egreso = Egreso.crearManual(
                LocalDate.of(2026, 6, 15),
                new BigDecimal("2000.00"),
                Concepto.UTE,
                FormaPago.TRANSFERENCIA,
                Procedencia.CAMPING,
                "Pago UTE"
        );

        Long id = finanzaRepository.saveAndFlush(egreso).getId();
        entityManager.clear();

        Finanza recuperada = finanzaRepository.findById(id).orElseThrow();

        assertInstanceOf(Egreso.class, recuperada);
        assertEquals(Concepto.UTE, recuperada.getConcepto());
        assertEquals(Procedencia.CAMPING, recuperada.getProcedencia());
        assertEquals(FormaPago.TRANSFERENCIA, recuperada.getFormaPago());
    }

    @Test
    void deberiaGuardarElDiscriminadorTipoSegunLaSubclase() {
        Long ingresoId = finanzaRepository.saveAndFlush(
                Ingreso.crearManual(LocalDate.now(), BigDecimal.TEN,
                        Concepto.PAGO_RESERVA, FormaPago.EFECTIVO, Procedencia.SEDE, null)
        ).getId();
        Long egresoId = finanzaRepository.saveAndFlush(
                Egreso.crearManual(LocalDate.now(), BigDecimal.TEN,
                        Concepto.UTE, FormaPago.EFECTIVO, Procedencia.SEDE, null)
        ).getId();

        assertEquals("INGRESO", tipoEnBaseDe(ingresoId));
        assertEquals("EGRESO", tipoEnBaseDe(egresoId));
    }

    @Test
    void deberiaMapearConceptoALaColumnaConceptoDePagoComoString() {
        Long id = finanzaRepository.saveAndFlush(
                Ingreso.crearManual(LocalDate.now(), BigDecimal.TEN,
                        Concepto.PAGO_RESERVA, FormaPago.EFECTIVO, Procedencia.SEDE, null)
        ).getId();

        Object concepto = entityManager.getEntityManager()
                .createNativeQuery("SELECT concepto_de_pago FROM finanza WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();

        assertEquals("PAGO_RESERVA", concepto);
    }

    @Test
    void noDeberiaPersistirCuandoLaProcedenciaEsNula() {
        Ingreso ingreso = Ingreso.crearManual(
                LocalDate.now(), BigDecimal.TEN,
                Concepto.PAGO_RESERVA, FormaPago.EFECTIVO, Procedencia.SEDE, null
        );
        ingreso.setProcedencia(null);

        assertThrows(DataIntegrityViolationException.class,
                () -> finanzaRepository.saveAndFlush(ingreso));
    }

    @Test
    void deberiaBorrarIngresosYEgresosDeLasReservasDadasEnUnDeleteMasivo() {
        Ingreso ingresoReserva1 = Ingreso.crearDesdeReserva(LocalDate.now(), BigDecimal.TEN,
                FormaPago.EFECTIVO, Procedencia.SEDE, null, 1L);
        Egreso egresoReserva1 = Egreso.crearDesdeCancelacionReserva(LocalDate.now(), BigDecimal.TEN,
                FormaPago.EFECTIVO, Procedencia.SEDE, null, 1L);
        Ingreso ingresoReserva2 = Ingreso.crearDesdeReserva(LocalDate.now(), BigDecimal.TEN,
                FormaPago.EFECTIVO, Procedencia.SEDE, null, 2L);
        Ingreso ingresoDeOtraReserva = Ingreso.crearDesdeReserva(LocalDate.now(), BigDecimal.TEN,
                FormaPago.EFECTIVO, Procedencia.SEDE, null, 99L);
        Long idAConservar = finanzaRepository.saveAndFlush(ingresoDeOtraReserva).getId();
        finanzaRepository.saveAndFlush(ingresoReserva1);
        finanzaRepository.saveAndFlush(egresoReserva1);
        finanzaRepository.saveAndFlush(ingresoReserva2);

        int ingresosBorrados = finanzaRepository.deleteIngresosByReservaIdIn(List.of(1L, 2L));
        int egresosBorrados = finanzaRepository.deleteEgresosByReservaIdIn(List.of(1L, 2L));
        entityManager.clear();

        assertEquals(2, ingresosBorrados);
        assertEquals(1, egresosBorrados);
        assertTrue(finanzaRepository.findById(idAConservar).isPresent());
    }

    @Test
    void deberiaBorrarSoloFinanzasSueltasConFechaAnteriorAlLimite() {
        LocalDate limite = LocalDate.now().minusYears(3);
        Ingreso ingresoSueltoVencido = Ingreso.crearManual(limite.minusDays(1), BigDecimal.TEN,
                Concepto.PAGO_RESERVA, FormaPago.EFECTIVO, Procedencia.SEDE, null);
        Ingreso ingresoSueltoVigente = Ingreso.crearManual(limite.plusDays(1), BigDecimal.TEN,
                Concepto.PAGO_RESERVA, FormaPago.EFECTIVO, Procedencia.SEDE, null);
        Egreso egresoSueltoVencido = Egreso.crearManual(limite.minusDays(1), BigDecimal.TEN,
                Concepto.UTE, FormaPago.EFECTIVO, Procedencia.SEDE, null);
        Ingreso ingresoDeReservaVencido = Ingreso.crearDesdeReserva(limite.minusDays(1), BigDecimal.TEN,
                FormaPago.EFECTIVO, Procedencia.SEDE, null, 1L);
        Long idVigente = finanzaRepository.saveAndFlush(ingresoSueltoVigente).getId();
        Long idDeReserva = finanzaRepository.saveAndFlush(ingresoDeReservaVencido).getId();
        finanzaRepository.saveAndFlush(ingresoSueltoVencido);
        finanzaRepository.saveAndFlush(egresoSueltoVencido);

        int ingresosBorrados = finanzaRepository.deleteIngresosSueltosConFechaAnteriorA(limite);
        int egresosBorrados = finanzaRepository.deleteEgresosSueltosConFechaAnteriorA(limite);
        entityManager.clear();

        assertEquals(1, ingresosBorrados);
        assertEquals(1, egresosBorrados);
        assertTrue(finanzaRepository.findById(idVigente).isPresent());
        assertTrue(finanzaRepository.findById(idDeReserva).isPresent(),
                "una finanza con reservaId no debe borrarse por la regla de finanzas sueltas, aunque sea vieja");
    }

    private String tipoEnBaseDe(Long id) {
        return (String) entityManager.getEntityManager()
                .createNativeQuery("SELECT tipo FROM finanza WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
    }
}
