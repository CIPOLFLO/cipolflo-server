package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservaService implements IReservaService {
    private static final int DIAS_VENTANA_RESERVAS_PROXIMAS = 60;

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }
    @Override
    public List<Reserva> obtenerProximasPorServicio(Long servicioId) {
        Instant desde = Instant.now();
        Instant hasta = desde.plus(
                DIAS_VENTANA_RESERVAS_PROXIMAS,
                ChronoUnit.DAYS
        );

        return reservaRepository.findByServicioIdAndFechaEntradaBetweenAndEstadoIn(
                servicioId,
                desde,
                hasta,
                List.of(
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA
                )
        );
    }

    @Override
    public List<Reserva> obtenerPorIdsYServicio(List<Long> ids, Long servicioId) {
        return List.of();
    }

    @Override
    public void cancelarTodas(List<Reserva> reservas) {

    }
}
