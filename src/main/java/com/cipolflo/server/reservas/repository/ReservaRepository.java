package com.cipolflo.server.reservas.repository;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByServicioIdAndFechaEntradaBetweenAndEstadoIn(
            Long servicioId,
            LocalDate desde,
            LocalDate hasta,
            Collection<EstadoReserva> estados
    );
    List<Reserva> findByClienteIdAndFechaEntradaAfterAndEstadoIn(
            Long clienteId,
            LocalDate desde,
            Collection<EstadoReserva> estados
    );

    List<Reserva> findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
            Long servicioId,
            Collection<EstadoReserva> estados,
            LocalDate hasta,
            LocalDate desde
    );

    boolean existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
            Long servicioId,
            Collection<EstadoReserva> estados,
            LocalDate fechaFin,
            LocalDate fechaInicio
    );
}
