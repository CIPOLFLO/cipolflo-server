package com.cipolflo.server.servicios.repository;

import com.cipolflo.server.servicios.domain.TarifaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarifaServicioRepository extends JpaRepository<TarifaServicio, Long> {

    List<TarifaServicio> findByServicioId(Long servicioId);

    Optional<TarifaServicio> findByIdAndServicioId(Long tarifaId, Long servicioId);
}