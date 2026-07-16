package com.cipolflo.server.reservas.domain.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoReservaTest {

    // ── esFinalizable ─────────────────────────────────────────────────────────

    @Test
    void esFinalizableDeberiaSerTrueSoloParaEnCursoYVencidaSinPago() {
        assertTrue(EstadoReserva.EN_CURSO.esFinalizable());
        assertTrue(EstadoReserva.VENCIDA_SIN_PAGO.esFinalizable());

        assertFalse(EstadoReserva.PENDIENTE.esFinalizable());
        assertFalse(EstadoReserva.CONFIRMADA.esFinalizable());
        assertFalse(EstadoReserva.FINALIZADA.esFinalizable());
        assertFalse(EstadoReserva.CANCELADA.esFinalizable());
    }

    // ── esOcupante ────────────────────────────────────────────────────────────

    @Test
    void esOcupanteDeberiaSerTrueParaPendienteConfirmadaYEnCurso() {
        assertTrue(EstadoReserva.PENDIENTE.esOcupante());
        assertTrue(EstadoReserva.CONFIRMADA.esOcupante());
        assertTrue(EstadoReserva.EN_CURSO.esOcupante());
    }

    @Test
    void esOcupanteDeberiaSerFalseParaVencidaSinPagoFinalizadaYCancelada() {
        // VENCIDA_SIN_PAGO ya pasó su fecha de salida: la estadía terminó, solo falta
        // cobrar, así que no debe bloquear nuevas reservas ni contar como ocupación.
        assertFalse(EstadoReserva.VENCIDA_SIN_PAGO.esOcupante());
        assertFalse(EstadoReserva.FINALIZADA.esOcupante());
        assertFalse(EstadoReserva.CANCELADA.esOcupante());
    }

    // ── ESTADOS_OCUPANTES ─────────────────────────────────────────────────────

    @Test
    void estadosOcupantesDeberiaContenerExactamentePendienteConfirmadaYEnCurso() {
        assertEquals(
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO),
                EstadoReserva.ESTADOS_OCUPANTES
        );
    }
}
