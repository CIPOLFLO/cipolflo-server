package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void deberiaBuscarYCancelarReservasFuturasDelCliente() {
        Long clienteId = 1L;

        Reserva reservaFutura = Reserva.crear(
                clienteId,
                10L,
                Procedencia.CAMPING,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                false
        );

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(Instant.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of(reservaFutura));

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        assertEquals(EstadoReserva.CANCELADA, reservaFutura.getEstado());

        verify(reservaRepository).findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(Instant.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        );

        verify(reservaRepository).saveAll(List.of(reservaFutura));
    }

    @Test
    void noDeberiaCancelarReservasPasadasDelCliente() {
        Long clienteId = 1L;

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(Instant.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of());

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        verify(reservaRepository).findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(Instant.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        );

        verify(reservaRepository).saveAll(List.of());
    }

    @Test
    void deberiaCancelarReservaPagaSinModificarPago() {
        Long clienteId = 1L;

        Reserva reservaPaga = Reserva.crear(
                clienteId,
                10L,
                Procedencia.CAMPING,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                false
        );

        reservaPaga.confirmarPago(
                BigDecimal.valueOf(1500),
                FormaPago.EFECTIVO
        );

        assertTrue(reservaPaga.getPago());

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(Instant.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of(reservaPaga));

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        assertEquals(EstadoReserva.CANCELADA, reservaPaga.getEstado());
        assertTrue(reservaPaga.getPago());

        verify(reservaRepository).saveAll(List.of(reservaPaga));
    }
}