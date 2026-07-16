package com.cipolflo.server.reservas.domain;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.PlazoConfirmacion;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                null,
                null,
                null,
                null,
                null,
                null,
                requiereDocumentacion,
                requiereSena,
                BigDecimal.valueOf(2000),
                null
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
                null,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                true,
                null,
                null
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

        reserva.registrarPago(BigDecimal.valueOf(1000), false);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());

        reserva.recibirDocumentacion();

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void recibirDocumentacionNoDeberiaCambiarEstadoSiYaEstaConfirmada() {
        Reserva reserva = crearComun(false, false);

        reserva.recibirDocumentacion();

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertTrue(reserva.getTieneDocumentacion());
    }

    // ── registrarPago ─────────────────────────────────────────────────────────

    @Test
    void registrarPagoDeberiaConfirmarCuandoNoRequiereDocumentacion() {
        Reserva reserva = crearComun(false, true);

        reserva.registrarPago(BigDecimal.valueOf(1000), false);

        assertFalse(reserva.getPago());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void registrarPagoNoDeberiaConfirmarSiRequiereDocumentacionYNoFueEntregada() {
        Reserva reserva = crearComun(true, true);

        reserva.registrarPago(BigDecimal.valueOf(1000), false);

        assertFalse(reserva.getPago());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void registrarPagoDeberiaConfirmarSiRequiereDocumentacionYYaFueEntregada() {
        Reserva reserva = crearComun(true, true);

        reserva.recibirDocumentacion();

        reserva.registrarPago(BigDecimal.valueOf(1000), false);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void registrarPagoTotalDeberiaMarcarReservaComoPaga() {
        Reserva reserva = crearComun(false, true);

        reserva.registrarPago(BigDecimal.valueOf(2000), true);

        assertTrue(reserva.getPago());
        assertEquals(BigDecimal.ZERO, reserva.getMontoImpago());
    }

    @Test
    void registrarPagoDeberiaLanzarExcepcionSiYaEstaPaga() {
        Reserva reserva = crearComun(false, true);

        reserva.registrarPago(BigDecimal.valueOf(2000), true);

        BigDecimal importe = BigDecimal.valueOf(100);
        assertThrows(
                IllegalStateException.class,
                () -> reserva.registrarPago(importe, true)
        );
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

        assertThrows(
                IllegalStateException.class,
                () -> reserva.cambiarEstado(EstadoReserva.CONFIRMADA)
        );
    }

    @Test
    void cambiarEstadoDeberiaLanzarExcepcionEnTransicionInvalidaDesdeCancelada() {
        Reserva reserva = crearComun(true, false);

        reserva.cancelar();

        assertThrows(
                IllegalStateException.class,
                () -> reserva.cambiarEstado(EstadoReserva.CONFIRMADA)
        );
    }

    @Test
    void cambiarEstadoDeberiaLanzarExcepcionDePendienteAEnCurso() {
        Reserva reserva = crearComun(true, false);

        assertThrows(
                IllegalStateException.class,
                () -> reserva.cambiarEstado(EstadoReserva.EN_CURSO)
        );
    }

    // ── modificar / cancelar ──────────────────────────────────────────────────

    @Test
    void modificarDeberiaActualizarCamposBasicos() {
        Reserva reserva = crearComun(false, false);

        reserva.modificar(
                20L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(8),
                4,
                1,
                null,
                "nota nueva"
        );

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
    // ── estaPaga ────────────────────────────────────────────────────────────────

    @Test
    void estaPagaDeberiaRetornarTrueCuandoMontoImpagoEsCero() {
        Reserva reserva = crearComun(false, true);

        reserva.registrarPago(BigDecimal.valueOf(2000), true);

        assertTrue(reserva.estaPaga());
    }

    @Test
    void estaPagaDeberiaRetornarFalseCuandoTieneMontoImpagoPendiente() {
        Reserva reserva = crearComun(false, true);

        reserva.registrarPago(BigDecimal.valueOf(500), false);

        assertFalse(reserva.estaPaga());
    }

    @Test
    void completarSaldoConPagoParcialNoDeberiaAlterarElImporteTotal() {
        // Reserva COMÚN de importe 2000: se paga una seña y luego se salda el resto.
        // Al saldar el monto impago (esPagoTotal=false) el importe total debe mantenerse.
        Reserva reserva = crearComun(false, true);

        reserva.registrarPago(BigDecimal.valueOf(500), false);
        assertEquals(BigDecimal.valueOf(1500), reserva.getMontoImpago());

        reserva.registrarPago(BigDecimal.valueOf(1500), false);

        assertTrue(reserva.getPago());
        assertTrue(reserva.estaPaga());
        assertEquals(BigDecimal.ZERO, reserva.getMontoImpago());
        assertEquals(BigDecimal.valueOf(2000), reserva.getImporte());
    }

    @Test
    void estaPagaDeberiaRetornarTrueCuandoEsColaboracionRecienCreada() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO,
                null,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                true,
                null,
                null
        );

        assertTrue(reserva.estaPaga());
    }

    // ── revertirPago ──────────────────────────────────────────────────────────

    @Test
    void revertirPagoDeberiaDevolverImporteAlMontoImpago() {
        Reserva reserva = crearComun(true, false);
        reserva.registrarPago(BigDecimal.valueOf(500), false);

        assertEquals(0, reserva.getMontoImpago().compareTo(BigDecimal.valueOf(1500)));

        reserva.revertirPago(BigDecimal.valueOf(500));

        assertEquals(0, reserva.getMontoImpago().compareTo(BigDecimal.valueOf(2000)));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void revertirPagoDeberiaPonerPagoEnFalseCuandoEstabaPaga() {
        Reserva reserva = crearComun(false, true);
        reserva.registrarPago(BigDecimal.valueOf(2000), true);

        assertTrue(reserva.getPago());

        reserva.revertirPago(BigDecimal.valueOf(2000));

        assertFalse(reserva.getPago());
        assertEquals(0, reserva.getMontoImpago().compareTo(BigDecimal.valueOf(2000)));
    }

    @Test
    void revertirPagoDeberiaVolverAPendienteSiRequiereSenaYQuedaBajoLaMitad() {
        Reserva reserva = crearComun(false, true);
        reserva.registrarPago(BigDecimal.valueOf(1000), false);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());

        reserva.revertirPago(BigDecimal.valueOf(1000));

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void revertirPagoNoDeberiaVolverAPendienteCuandoNoRequiereSena() {
        Reserva reserva = crearComun(false, false);
        reserva.registrarPago(BigDecimal.valueOf(500), false);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());

        reserva.revertirPago(BigDecimal.valueOf(500));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void revertirPagoNoDeberiaVolverAPendienteSiSigueCumpliendoLaMitad() {
        Reserva reserva = crearComun(false, true);
        reserva.registrarPago(BigDecimal.valueOf(2000), false);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());

        reserva.revertirPago(BigDecimal.valueOf(500));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void revertirPagoNoDeberiaSuperarElImporteTotal() {
        Reserva reserva = crearComun(true, false);
        reserva.registrarPago(BigDecimal.valueOf(800), false);

        reserva.revertirPago(BigDecimal.valueOf(5000));

        assertEquals(0, reserva.getMontoImpago().compareTo(BigDecimal.valueOf(2000)));
    }

    @Test
    void revertirPagoEnCursoNoDeberiaCambiarElEstado() {
        Reserva reserva = crearComun(false, false);
        reserva.registrarPago(BigDecimal.valueOf(2000), true);
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);

        reserva.revertirPago(BigDecimal.valueOf(2000));

        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertFalse(reserva.getPago());
        assertEquals(0, reserva.getMontoImpago().compareTo(BigDecimal.valueOf(2000)));
    }

    @Test
    void cambiarEstadoDeberiaPermitirConfirmadaAPendiente() {
        Reserva reserva = crearComun(false, false);

        reserva.cambiarEstado(EstadoReserva.PENDIENTE);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }
    // ── PlazoConfirmacion / fechaLimiteConfirmacion ──────────────────────────

    private Reserva crearConPlazo(PlazoConfirmacion plazoConfirmacion, LocalDate fechaEntrada, LocalTime horaInicio) {
        return Reserva.crear(
                TipoReserva.COMUN,
                5L,
                10L,
                Procedencia.CAMPING,
                fechaEntrada,
                fechaEntrada.plusDays(2),
                horaInicio,
                horaInicio != null ? horaInicio.plusHours(1) : null,
                null,
                null,
                null,
                null,
                true,
                true,
                BigDecimal.valueOf(2000),
                plazoConfirmacion
        );
    }

    @Test
    void crearSinPlazoConfirmacionDejaFechaLimiteConfirmacionNull() {
        Reserva reserva = crearComun(true, true);

        assertNull(reserva.getPlazoConfirmacion());
        assertNull(reserva.getFechaLimiteConfirmacion());
        assertNull(reserva.getFechaInicioAlerta());
    }

    @Test
    void crearConPlazoVeinticuatroHorasCalculaFechaLimiteConfirmacionSinHoraInicio() {
        LocalDate fechaEntrada = LocalDate.of(2026, 7, 13);
        Reserva reserva = crearConPlazo(PlazoConfirmacion.VEINTICUATRO_HORAS, fechaEntrada, null);

        assertEquals(PlazoConfirmacion.VEINTICUATRO_HORAS, reserva.getPlazoConfirmacion());
        assertEquals(LocalDateTime.of(2026, 7, 12, 0, 0), reserva.getFechaLimiteConfirmacion());
    }

    @Test
    void crearConPlazoUsaHoraInicioParaCalcularElInicioDeLaReserva() {
        LocalDate fechaEntrada = LocalDate.of(2026, 7, 13);
        Reserva reserva = crearConPlazo(PlazoConfirmacion.VEINTICUATRO_HORAS, fechaEntrada, LocalTime.of(14, 0));

        // inicioReserva = 13-jul 14:00 → límite = 12-jul 14:00 (no medianoche)
        assertEquals(LocalDateTime.of(2026, 7, 12, 14, 0), reserva.getFechaLimiteConfirmacion());
    }

    @Test
    void crearConPlazoTresMesesReproduceElEjemploDelTicket() {
        LocalDate fechaEntrada = LocalDate.of(2026, 6, 7);
        Reserva reserva = crearConPlazo(PlazoConfirmacion.TRES_MESES, fechaEntrada, null);

        assertEquals(LocalDateTime.of(2026, 3, 7, 0, 0), reserva.getFechaLimiteConfirmacion());
        assertEquals(LocalDateTime.of(2026, 2, 28, 0, 0), reserva.getFechaInicioAlerta());
    }

    @Test
    void getFechaInicioAlertaEsNullCuandoNoHayPlazoConfirmacion() {
        Reserva reserva = crearComun(false, false);

        assertNull(reserva.getFechaInicioAlerta());
    }

    @Test
    void modificarRecalculaFechaLimiteConfirmacionSegunLaNuevaFechaEntrada() {
        LocalDate fechaEntradaOriginal = LocalDate.of(2026, 7, 13);
        Reserva reserva = crearConPlazo(PlazoConfirmacion.VEINTICUATRO_HORAS, fechaEntradaOriginal, null);
        assertEquals(LocalDateTime.of(2026, 7, 12, 0, 0), reserva.getFechaLimiteConfirmacion());

        LocalDate nuevaFechaEntrada = LocalDate.of(2026, 8, 20);
        reserva.modificar(
                20L,
                Procedencia.CAMPING,
                nuevaFechaEntrada,
                nuevaFechaEntrada.plusDays(2),
                4,
                1,
                null,
                "nota nueva"
        );

        assertEquals(LocalDateTime.of(2026, 8, 19, 0, 0), reserva.getFechaLimiteConfirmacion());
    }

    @Test
    void modificarSinPlazoConfirmacionMantieneFechaLimiteConfirmacionNull() {
        Reserva reserva = crearComun(false, false);
        assertNull(reserva.getFechaLimiteConfirmacion());

        reserva.modificar(
                20L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                4,
                1,
                null,
                "nota nueva"
        );

        assertNull(reserva.getFechaLimiteConfirmacion());
    }
}