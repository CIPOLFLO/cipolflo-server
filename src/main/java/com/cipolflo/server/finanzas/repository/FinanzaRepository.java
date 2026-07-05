package com.cipolflo.server.finanzas.repository;

import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FinanzaRepository extends JpaRepository<Finanza, Long>, JpaSpecificationExecutor<Finanza> {

    @Query("SELECT i FROM Ingreso i WHERE i.reservaId = :reservaId")
    List<Ingreso> findIngresosByReservaId(@Param("reservaId") Long reservaId);

}
