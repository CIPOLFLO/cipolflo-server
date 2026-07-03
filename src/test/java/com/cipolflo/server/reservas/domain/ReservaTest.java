package com.cipolflo.server.reservas.domain;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private Reserva crearComun(boolean requiereDocumentacion, boolean requiereSena) {
        return Reserva.crear(
                TipoReserva.COMUN,
                5L,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                null, null, null, null, null, null, null, null,
                requiereDocumentacion,
                requiereSena
        );
    }

    // ── resolverEstado / Reserva.crear ───────────────────────────────────────

    @Test
    void deberiaQuedarConfirmadaCuandoNoRequiereNadaYEsComun() {
        Reserva reserva = crearComun(false, false);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void deberiaQuedarPendienteCuandoRequiereSoloDocumentacion() {
        Reserva reserva = crearComun(true, false);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void deberiaQuedarPendienteCuandoRequiereSoloSena() {
        Reserva reserva = crearComun(false, true);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void deberiaQuedarPendienteCuandoRequiereDocumentacionYSena() {
        Reserva reserva = crearComun(true, true);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void deberiaQuedarConfirmadaSiEsColaboracionAunRequiriendoDocumentacionYSena() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO,
                null, 10L, Procedencia.CAMPING,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, null, null, null, null, "20123456-7", "Org Test", null,
                true, true
        );
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    // ── recibirDocumentacion ──────────────────────────────────────────────────

    @Test
    void recibirDocumentacionDeberiaConfirmarCuandoNoRequiereSena() {
        Reserva reserva = crearComun(true, false);

        reserva.recibirDocumentacion();

        assertTrue(reserva.getTieneDocumentacion());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void recibirDocumentacionNoDeberiaConfirmarSiRequiereSenaYNoFuePagada() {
        Reserva reserva = crearComun(true, true);

        reserva.recibirDocumentacion();

        assertTrue(reserva.getTieneDocumentacion());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void recibirDocumentacionDeberiaConfirmarSiRequiereSenaYYaFuePagada() {
        Reserva reserva = crearComun(true, true);
        reserva.confirmarPago(BigDecimal.valueOf(1000), FormaPago.EFECTIVO);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());

        reserva.recibirDocumentacion();

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void recibirDocumentacionNoDeberiaCambiarEstadoSiYaEstaConfirmada() {
        Reserva reserva = crearComun(false, false);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());

        reserva.recibirDocumentacion();

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertTrue(reserva.getTieneDocumentacion());
    }

    // ── confirmarPago ─────────────────────────────────────────────────────────

    @Test
    void confirmarPagoDeberiaConfirmarCuandoNoRequiereDocumentacion() {
        Reserva reserva = crearComun(false, true);

        reserva.confirmarPago(BigDecimal.valueOf(1500), FormaPago.EFECTIVO);

        assertTrue(reserva.getPago());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void confirmarPagoNoDeberiaConfirmarSiRequiereDocumentacionYNoFueEntregada() {
        Reserva reserva = crearComun(true, true);

        reserva.confirmarPago(BigDecimal.valueOf(1500), FormaPago.EFECTIVO);

        assertTrue(reserva.getPago());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void confirmarPagoDeberiaConfirmarSiRequiereDocumentacionYYaFueEntregada() {
        Reserva reserva = crearComun(true, true);
        reserva.recibirDocumentacion();
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());

        reserva.confirmarPago(BigDecimal.valueOf(1500), FormaPago.EFECTIVO);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void confirmarPagoDeberiaLanzarExcepcionSiYaEstaPaga() {
        Reserva reserva = crearComun(false, true);
        reserva.confirmarPago(BigDecimal.valueOf(1500), FormaPago.EFECTIVO);

        assertThrows(IllegalStateException.class,
                () -> reserva.confirmarPago(BigDecimal.valueOf(1500), FormaPago.EFECTIVO));
    }

    @Test
    void confirmarPagoDeberiaLanzarExcepcionSiImporteEsNulo() {
        Reserva reserva = crearComun(false, true);

        assertThrows(IllegalArgumentException.class,
                () -> reserva.confirmarPago(null, FormaPago.EFECTIVO));
    }

    @Test
    void confirmarPagoDeberiaLanzarExcepcionSiFormaPagoEsNula() {
        Reserva reserva = crearComun(false, true);

        assertThrows(IllegalArgumentException.class,
                () -> reserva.confirmarPago(BigDecimal.valueOf(1000), null));
    }

    @Test
    void confirmarPagoDeberiaLanzarExcepcionSiImporteEsCeroONegativo() {
        Reserva reserva = crearComun(false, true);

        assertThrows(IllegalArgumentException.class,
                () -> reserva.confirmarPago(BigDecimal.ZERO, FormaPago.EFECTIVO));
    }

    // ── cambiarEstado / transiciones ──────────────────────────────────────────

    @Test
    void cambiarEstadoDeberiaPermitirPendienteAConfirmada() {
        Reserva reserva = crearComun(true, false);
        reserva.cambiarEstado(EstadoReserva.CONFIRMADA);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void cambiarEstadoDeberiaPermitirPendienteACancelada() {
        Reserva reserva = crearComun(true, false);
        reserva.cambiarEstado(EstadoReserva.CANCELADA);
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }

    @Test
    void cambiarEstadoDeberiaPermitirConfirmadaAEnCurso() {
        Reserva reserva = crearComun(false, false);
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
    }

    @Test
    void cambiarEstadoDeberiaPermitirEnCursoAFinalizada() {
        Reserva reserva = crearComun(false, false);
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        reserva.cambiarEstado(EstadoReserva.FINALIZADA);
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
    }

    @Test
    void cambiarEstadoDeberiaLanzarExcepcionEnTransicionInvalidaDesdeFinalizada() {
        Reserva reserva = crearComun(false, false);
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        reserva.cambiarEstado(EstadoReserva.FINALIZADA);

        assertThrows(IllegalStateException.class,
                () -> reserva.cambiarEstado(EstadoReserva.CONFIRMADA));
    }

    @Test
    void cambiarEstadoDeberiaLanzarExcepcionEnTransicionInvalidaDesdeCancelada() {
        Reserva reserva = crearComun(true, false);
        reserva.cancelar();

        assertThrows(IllegalStateException.class,
                () -> reserva.cambiarEstado(EstadoReserva.CONFIRMADA));
    }

    @Test
    void cambiarEstadoDeberiaLanzarExcepcionDePendienteAEnCurso() {
        Reserva reserva = crearComun(true, false);

        assertThrows(IllegalStateException.class,
                () -> reserva.cambiarEstado(EstadoReserva.EN_CURSO));
    }

    // ── modificar / cancelar ──────────────────────────────────────────────────

    @Test
    void modificarDeberiaActualizarCamposBasicos() {
        Reserva reserva = crearComun(false, false);

        reserva.modificar(20L, Procedencia.CAMPING, LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(8), 4, 1, null, null, "nota nueva");

        assertEquals(20L, reserva.getServicioId());
        assertEquals(4, reserva.getCantidadTotal());
        assertEquals("nota nueva", reserva.getNotas());
    }

    @Test
    void cancelarDeberiaCambiarEstadoACancelada() {
        Reserva reserva = crearComun(false, false);
        reserva.cancelar();
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }
}