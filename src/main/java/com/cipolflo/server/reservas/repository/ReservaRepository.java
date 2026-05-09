package com.cipolflo.server.reservas.repository;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByServicioIdAndFechaEntradaBetweenAndEstadoIn(
            Long servicioId,
            Instant desde,
            Instant hasta,
            Collection<EstadoReserva> estados
    );

    List<Reserva> findByIdInAndServicioId(
            List<Long> ids,
            Long servicioId
    );
}
