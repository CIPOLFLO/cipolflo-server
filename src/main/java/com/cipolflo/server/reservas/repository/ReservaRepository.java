package com.cipolflo.server.reservas.repository;

import com.cipolflo.server.reservas.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
}
