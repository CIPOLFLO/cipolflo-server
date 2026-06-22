package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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
                TipoReserva.COMUN,
                clienteId,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null,
                false
        );

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of(reservaFutura));

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        assertEquals(EstadoReserva.CANCELADA, reservaFutura.getEstado());

        verify(reservaRepository).findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        );

        verify(reservaRepository).saveAll(List.of(reservaFutura));
    }

    @Test
    void noDeberiaCancelarReservasPasadasDelCliente() {
        Long clienteId = 1L;

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of());

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        verify(reservaRepository).findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        );

        verify(reservaRepository).saveAll(List.of());
    }

    @Test
    void deberiaCancelarReservaPagaSinModificarPago() {
        Long clienteId = 1L;

        Reserva reservaPaga = Reserva.crear(
                TipoReserva.COMUN,
                clienteId,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null,
                false
        );

        reservaPaga.confirmarPago(
                BigDecimal.valueOf(1500),
                FormaPago.EFECTIVO
        );

        assertTrue(reservaPaga.getPago());

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of(reservaPaga));

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        assertEquals(EstadoReserva.CANCELADA, reservaPaga.getEstado());
        assertTrue(reservaPaga.getPago());

        verify(reservaRepository).saveAll(List.of(reservaPaga));
    }

    @Test
    void deberiaObtenerOcupacionPorServicioEnRangoConSolapamientoInclusivo() {
        Long servicioId = 10L;
        LocalDate desde = LocalDate.of(2026, 6, 16);
        LocalDate hasta = LocalDate.of(2026, 6, 20);

        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                1L,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.of(2026, 6, 18),
                LocalDate.of(2026, 6, 19),
                null, null, null, null, null,
                false
        );

        when(reservaRepository
                .findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        eq(servicioId),
                        eq(List.of(
                                EstadoReserva.PENDIENTE,
                                EstadoReserva.CONFIRMADA,
                                EstadoReserva.EN_CURSO
                        )),
                        eq(hasta),
                        eq(desde)
                )).thenReturn(List.of(reserva));

        List<Reserva> resultado =
                reservaService.obtenerOcupacionPorServicioEnRango(servicioId, desde, hasta);

        assertEquals(1, resultado.size());
        assertEquals(reserva, resultado.get(0));

        verify(reservaRepository)
                .findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        eq(servicioId),
                        eq(List.of(
                                EstadoReserva.PENDIENTE,
                                EstadoReserva.CONFIRMADA,
                                EstadoReserva.EN_CURSO
                        )),
                        eq(hasta),
                        eq(desde)
                );
    }
}
