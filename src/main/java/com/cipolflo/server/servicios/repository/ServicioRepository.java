package com.cipolflo.server.servicios.repository;

import com.cipolflo.server.servicios.domain.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServicioRepository extends JpaRepository<Servicio, Long>, JpaSpecificationExecutor<Servicio> {
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
