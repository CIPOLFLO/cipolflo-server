package com.cipolflo.server.finanzas.repository;

import com.cipolflo.server.finanzas.domain.Finanza;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanzaRepository extends JpaRepository<Finanza, Long> {
}
