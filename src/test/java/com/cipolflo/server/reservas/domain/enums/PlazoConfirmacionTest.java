package com.cipolflo.server.reservas.domain.enums;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlazoConfirmacionTest {

    // ── VEINTICUATRO_HORAS ────────────────────────────────────────────────────

    @Test
    void veinticuatroHorasDeberiaCalcularFechaLimiteConfirmacion24HorasAntesDelInicio() {
        LocalDateTime inicioReserva = LocalDateTime.of(2026, 7, 13, 0, 0);

        LocalDateTime fechaLimite = PlazoConfirmacion.VEINTICUATRO_HORAS
                .calcularFechaLimiteConfirmacion(inicioReserva);

        assertEquals(LocalDateTime.of(2026, 7, 12, 0, 0), fechaLimite);
    }

    @Test
    void veinticuatroHorasDeberiaCalcularFechaInicioAlerta24HorasAntesDelLimite() {
        LocalDateTime fechaLimiteConfirmacion = LocalDateTime.of(2026, 7, 12, 0, 0);

        LocalDateTime fechaInicioAlerta = PlazoConfirmacion.VEINTICUATRO_HORAS
                .calcularFechaInicioAlerta(fechaLimiteConfirmacion);

        assertEquals(LocalDateTime.of(2026, 7, 11, 0, 0), fechaInicioAlerta);
    }

    // ── TRES_MESES ────────────────────────────────────────────────────────────

    @Test
    void tresMesesDeberiaCalcularFechaLimiteConfirmacion3MesesAntesDelInicio() {
        LocalDateTime inicioReserva = LocalDateTime.of(2026, 6, 7, 0, 0);

        LocalDateTime fechaLimite = PlazoConfirmacion.TRES_MESES
                .calcularFechaLimiteConfirmacion(inicioReserva);

        assertEquals(LocalDateTime.of(2026, 3, 7, 0, 0), fechaLimite);
    }

    @Test
    void tresMesesDeberiaCalcularFechaInicioAlerta7DiasAntesDelLimite() {
        LocalDateTime fechaLimiteConfirmacion = LocalDateTime.of(2026, 3, 7, 0, 0);

        LocalDateTime fechaInicioAlerta = PlazoConfirmacion.TRES_MESES
                .calcularFechaInicioAlerta(fechaLimiteConfirmacion);

        assertEquals(LocalDateTime.of(2026, 2, 28, 0, 0), fechaInicioAlerta);
    }

    @Test
    void tresMesesDeberiaReproducirElEjemploCompletoDelTicket() {
        // Ejemplo del ticket: inicio = 7-jun-2026 → límite ≈ 7-mar-2026 → alerta desde ~28-feb-2026.
        LocalDateTime inicioReserva = LocalDateTime.of(2026, 6, 7, 0, 0);

        LocalDateTime fechaLimiteConfirmacion = PlazoConfirmacion.TRES_MESES
                .calcularFechaLimiteConfirmacion(inicioReserva);
        LocalDateTime fechaInicioAlerta = PlazoConfirmacion.TRES_MESES
                .calcularFechaInicioAlerta(fechaLimiteConfirmacion);

        assertEquals(LocalDateTime.of(2026, 3, 7, 0, 0), fechaLimiteConfirmacion);
        assertEquals(LocalDateTime.of(2026, 2, 28, 0, 0), fechaInicioAlerta);
    }

    // ── labels ────────────────────────────────────────────────────────────────

    @Test
    void veinticuatroHorasDeberiaTenerLabel24Horas() {
        assertEquals("24 horas", PlazoConfirmacion.VEINTICUATRO_HORAS.getLabel());
        assertEquals("24 horas", PlazoConfirmacion.VEINTICUATRO_HORAS.toString());
    }

    @Test
    void tresMesesDeberiaTenerLabel3Meses() {
        assertEquals("3 meses", PlazoConfirmacion.TRES_MESES.getLabel());
        assertEquals("3 meses", PlazoConfirmacion.TRES_MESES.toString());
    }
}