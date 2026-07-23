package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.PlazoConfirmacion;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.events.MotivoCancelacionReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelacionAutomaticaReservasServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private IReservaService reservaService;

    @InjectMocks
    private CancelacionAutomaticaReservasService service;

    private Reserva crearReservaPendienteVencida() {
        return Reserva.crear(
                TipoReserva.COMUN,
                5L,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                true,
                BigDecimal.valueOf(2000),
                PlazoConfirmacion.VEINTICUATRO_HORAS
        );
    }

    @Test
    void filtraSoloReservasEnEstadoPendienteConFechaLimiteVencida() {
        Reserva vencida = crearReservaPendienteVencida();
        when(reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(
                eq(EstadoReserva.PENDIENTE), any(LocalDateTime.class)
        )).thenReturn(List.of(vencida));

        service.cancelarReservasVencidas();

        verify(reservaRepository).findByEstadoAndFechaLimiteConfirmacionLessThanEqual(
                eq(EstadoReserva.PENDIENTE), any(LocalDateTime.class)
        );
    }

    @Test
    void cancelaTodasLasReservasVencidasEncontradasConElMotivoDeVencimientoDePlazo() {
        Reserva vencida1 = crearReservaPendienteVencida();
        Reserva vencida2 = crearReservaPendienteVencida();
        List<Reserva> vencidas = List.of(vencida1, vencida2);
        when(reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(
                any(), any()
        )).thenReturn(vencidas);

        service.cancelarReservasVencidas();

        verify(reservaService).cancelarTodas(vencidas, MotivoCancelacionReserva.VENCIMIENTO_PLAZO_CONFIRMACION);
    }

    @Test
    void devuelveResumenConLaCantidadDeReservasCanceladas() {
        when(reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(any(), any()))
                .thenReturn(List.of(crearReservaPendienteVencida(), crearReservaPendienteVencida(), crearReservaPendienteVencida()));

        String resumen = service.cancelarReservasVencidas();

        assertTrue(resumen.contains("3"));
        assertEquals("3 reservas canceladas automáticamente", resumen);
    }

    @Test
    void devuelveResumenDeCeroCuandoNoHayReservasVencidas() {
        when(reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(any(), any()))
                .thenReturn(List.of());

        String resumen = service.cancelarReservasVencidas();

        assertEquals("0 reservas canceladas automáticamente", resumen);
        verify(reservaService, never()).cancelarTodas(any(), any());
    }

    @Test
    void noLlamaCancelarTodasCuandoLaListaEstaVacia() {
        when(reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(any(), any()))
                .thenReturn(List.of());

        service.cancelarReservasVencidas();

        verify(reservaService, never()).cancelarTodas(any(), any());
    }
}