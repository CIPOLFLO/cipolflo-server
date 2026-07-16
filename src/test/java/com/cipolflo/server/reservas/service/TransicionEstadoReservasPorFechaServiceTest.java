package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransicionEstadoReservasPorFechaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private TransicionEstadoReservasPorFechaService service;

    private Reserva reservaMock(EstadoReserva estado, boolean paga) {
        Reserva r = mock(Reserva.class);
        lenient().when(r.getEstado()).thenReturn(estado);
        lenient().when(r.estaPaga()).thenReturn(paga);
        return r;
    }

    @Test
    void deberiaPasarConfirmadasAEnCursoCuandoHoyEsLaFechaDeEntrada() {
        LocalDate hoy = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate ayer = hoy.minusDays(1);
        Reserva aIniciar = reservaMock(EstadoReserva.CONFIRMADA, false);

        when(reservaRepository.findByEstadoAndFechaEntrada(EstadoReserva.CONFIRMADA, hoy))
                .thenReturn(List.of(aIniciar));
        when(reservaRepository.findByEstadoAndFechaSalida(EstadoReserva.EN_CURSO, ayer))
                .thenReturn(List.of());

        String resumen = service.transicionarEstadosPorFecha();

        verify(aIniciar).cambiarEstado(EstadoReserva.EN_CURSO);
        verify(reservaRepository).saveAll(List.of(aIniciar));
        assertEquals("1 iniciadas, 0 finalizadas, 0 vencidas sin pago.", resumen);
    }

    @Test
    void deberiaFinalizarEnCursoPagaCuandoAyerFueLaFechaDeSalida() {
        LocalDate hoy = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate ayer = hoy.minusDays(1);
        Reserva pagaAlDia = reservaMock(EstadoReserva.EN_CURSO, true);

        when(reservaRepository.findByEstadoAndFechaEntrada(EstadoReserva.CONFIRMADA, hoy))
                .thenReturn(List.of());
        when(reservaRepository.findByEstadoAndFechaSalida(EstadoReserva.EN_CURSO, ayer))
                .thenReturn(List.of(pagaAlDia));

        String resumen = service.transicionarEstadosPorFecha();

        verify(pagaAlDia).cambiarEstado(EstadoReserva.FINALIZADA);
        assertEquals("0 iniciadas, 1 finalizadas, 0 vencidas sin pago.", resumen);
    }

    @Test
    void deberiaMarcarVencidaSinPagoCuandoAyerFueLaFechaDeSalidaYNoEstaPaga() {
        LocalDate hoy = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate ayer = hoy.minusDays(1);
        Reserva impaga = reservaMock(EstadoReserva.EN_CURSO, false);

        when(reservaRepository.findByEstadoAndFechaEntrada(EstadoReserva.CONFIRMADA, hoy))
                .thenReturn(List.of());
        when(reservaRepository.findByEstadoAndFechaSalida(EstadoReserva.EN_CURSO, ayer))
                .thenReturn(List.of(impaga));

        String resumen = service.transicionarEstadosPorFecha();

        verify(impaga).cambiarEstado(EstadoReserva.VENCIDA_SIN_PAGO);
        assertEquals("0 iniciadas, 0 finalizadas, 1 vencidas sin pago.", resumen);
    }

    @Test
    void noDeberiaTocarNadaCuandoNoHayReservasParaTransicionar() {
        LocalDate hoy = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate ayer = hoy.minusDays(1);

        when(reservaRepository.findByEstadoAndFechaEntrada(EstadoReserva.CONFIRMADA, hoy))
                .thenReturn(List.of());
        when(reservaRepository.findByEstadoAndFechaSalida(EstadoReserva.EN_CURSO, ayer))
                .thenReturn(List.of());

        String resumen = service.transicionarEstadosPorFecha();

        assertEquals("0 iniciadas, 0 finalizadas, 0 vencidas sin pago.", resumen);
    }
}
