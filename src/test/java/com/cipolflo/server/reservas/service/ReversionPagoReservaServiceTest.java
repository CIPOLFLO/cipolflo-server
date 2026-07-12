package com.cipolflo.server.reservas.service;

import com.cipolflo.server.finanzas.exception.ConfirmacionEliminacionReservaRequeridaException;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReversionPagoReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReversionPagoReservaService service;

    @Test
    void deberiaLanzarNotFoundCuandoLaReservaNoExiste() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ReservaNotFoundException.class,
                () -> service.revertirPorEliminacion(99L, BigDecimal.valueOf(1000), false)
        );

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deberiaRevertirYGuardarCuandoLaReservaEstaPendiente() {
        Reserva reserva = crearPendiente();
        reserva.registrarPago(BigDecimal.valueOf(500), false);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.revertirPorEliminacion(1L, BigDecimal.valueOf(500), false);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(0, reserva.getMontoImpago().compareTo(BigDecimal.valueOf(2000)));
        verify(reservaRepository).save(reserva);
    }

    @Test
    void deberiaVolverAPendienteCuandoConfirmadaConSenaCaeBajoLaMitad() {
        Reserva reserva = crearComun(false, true);
        reserva.registrarPago(BigDecimal.valueOf(1000), false);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.revertirPorEliminacion(1L, BigDecimal.valueOf(1000), false);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void deberiaPermanecerConfirmadaCuandoNoRequiereSena() {
        Reserva reserva = crearComun(false, false);
        reserva.registrarPago(BigDecimal.valueOf(500), false);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.revertirPorEliminacion(1L, BigDecimal.valueOf(500), false);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void deberiaRevertirSinCambiarEstadoCuandoEstaEnCurso() {
        Reserva reserva = crearComun(false, false);
        reserva.registrarPago(BigDecimal.valueOf(2000), true);
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.revertirPorEliminacion(1L, BigDecimal.valueOf(2000), false);

        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertFalse(reserva.getPago());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void deberiaLanzarConfirmacionRequeridaCuandoFinalizadaSinConfirmar() {
        Reserva reserva = crearFinalizada();
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThrows(
                ConfirmacionEliminacionReservaRequeridaException.class,
                () -> service.revertirPorEliminacion(1L, BigDecimal.valueOf(1000), false)
        );

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void noDeberiaModificarReservaFinalizadaCuandoConfirmar() {
        Reserva reserva = crearFinalizada();
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.revertirPorEliminacion(1L, BigDecimal.valueOf(1000), true);

        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarConfirmacionRequeridaCuandoCanceladaSinConfirmar() {
        Reserva reserva = crearCancelada();
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThrows(
                ConfirmacionEliminacionReservaRequeridaException.class,
                () -> service.revertirPorEliminacion(1L, BigDecimal.valueOf(1000), false)
        );

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void noDeberiaModificarReservaCanceladaCuandoConfirmar() {
        Reserva reserva = crearCancelada();
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.revertirPorEliminacion(1L, BigDecimal.valueOf(1000), true);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        verify(reservaRepository, never()).save(any());
    }

    private Reserva crearComun(boolean requiereDocumentacion, boolean requiereSena) {
        return Reserva.crear(
                TipoReserva.COMUN,
                1L,
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

    private Reserva crearPendiente() {
        return crearComun(true, false);
    }

    private Reserva crearFinalizada() {
        Reserva reserva = crearComun(false, false);
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        reserva.cambiarEstado(EstadoReserva.FINALIZADA);
        return reserva;
    }

    private Reserva crearCancelada() {
        Reserva reserva = crearComun(true, false);
        reserva.cancelar();
        return reserva;
    }
}
