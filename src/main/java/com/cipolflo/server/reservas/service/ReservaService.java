package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaService implements IReservaService {
    private static final int DIAS_VENTANA_RESERVAS_PROXIMAS = 60;

    private static final List<EstadoReserva> ESTADOS_OCUPANTES = List.of(
            EstadoReserva.PENDIENTE,
            EstadoReserva.CONFIRMADA,
            EstadoReserva.EN_CURSO
    );

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Override
    public List<Reserva> obtenerProximasPorServicioEnRango(Long servicioId) {
        LocalDate desde = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate hasta = desde.plusDays(DIAS_VENTANA_RESERVAS_PROXIMAS);

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
    public List<Reserva> obtenerOcupacionPorServicioEnRango(Long servicioId, LocalDate desde, LocalDate hasta) {
        return reservaRepository
                .findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        servicioId,
                        ESTADOS_OCUPANTES,
                        hasta,
                        desde
                );
    }

    @Override
    public void cancelarTodas(List<Reserva> reservas) {
        reservas.forEach(Reserva::cancelar);
        reservaRepository.saveAll(reservas);
    }
    @Override
    public void cancelarReservasFuturasPorCliente(Long clienteId) {
        List<Reserva> reservas = reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                clienteId,
                LocalDate.now(ZonaHoraria.URUGUAY),
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );

        cancelarTodas(reservas);
    }
}
