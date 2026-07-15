package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransicionEstadoReservasPorFechaService implements ITransicionEstadoReservasPorFechaService {

    private final ReservaRepository reservaRepository;

    public TransicionEstadoReservasPorFechaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional
    public String transicionarEstadosPorFecha() {
        LocalDate hoy = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate ayer = hoy.minusDays(1);

        List<Reserva> aIniciar = reservaRepository.findByEstadoAndFechaEntrada(EstadoReserva.CONFIRMADA, hoy);
        aIniciar.forEach(reserva -> reserva.cambiarEstado(EstadoReserva.EN_CURSO));
        reservaRepository.saveAll(aIniciar);

        List<Reserva> aCerrar = reservaRepository.findByEstadoAndFechaSalida(EstadoReserva.EN_CURSO, ayer);
        int finalizadas = 0;
        int vencidasSinPago = 0;
        for (Reserva reserva : aCerrar) {
            if (reserva.estaPaga()) {
                reserva.cambiarEstado(EstadoReserva.FINALIZADA);
                finalizadas++;
            } else {
                reserva.cambiarEstado(EstadoReserva.VENCIDA_SIN_PAGO);
                vencidasSinPago++;
            }
        }
        reservaRepository.saveAll(aCerrar);

        return "%d iniciadas, %d finalizadas, %d vencidas sin pago.".formatted(
                aIniciar.size(), finalizadas, vencidasSinPago);
    }
}
