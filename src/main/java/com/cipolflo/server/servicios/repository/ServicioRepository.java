package com.cipolflo.server.servicios.repository;

import com.cipolflo.server.servicios.domain.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
}
