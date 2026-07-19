package com.cipolflo.server.finanzas.repository;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FinanzaRepository extends JpaRepository<Finanza, Long>, JpaSpecificationExecutor<Finanza> {

    @Query("SELECT i FROM Ingreso i WHERE i.reservaId = :reservaId")
    List<Ingreso> findIngresosByReservaId(@Param("reservaId") Long reservaId);

    /**
     * Borra en un único {@code DELETE} masivo los ingresos asociados a alguna de las
     * reservas en {@code reservaIds}. Usado por la limpieza de reservas vencidas.
     */
    @Modifying
    @Query("DELETE FROM Ingreso i WHERE i.reservaId IN :reservaIds")
    int deleteIngresosByReservaIdIn(@Param("reservaIds") List<Long> reservaIds);

    /**
     * Borra en un único {@code DELETE} masivo los egresos asociados a alguna de las
     * reservas en {@code reservaIds}. Usado por la limpieza de reservas vencidas.
     */
    @Modifying
    @Query("DELETE FROM Egreso e WHERE e.reservaId IN :reservaIds")
    int deleteEgresosByReservaIdIn(@Param("reservaIds") List<Long> reservaIds);

    /**
     * Borra en un único {@code DELETE} masivo los ingresos sin reserva asociada
     * ({@code reservaId} nulo) cuya fecha es anterior o igual a {@code limite}.
     */
    @Modifying
    @Query("DELETE FROM Ingreso i WHERE i.reservaId IS NULL AND i.fecha <= :limite")
    int deleteIngresosSueltosConFechaAnteriorA(@Param("limite") LocalDate limite);

    /**
     * Borra en un único {@code DELETE} masivo los egresos sin reserva asociada
     * ({@code reservaId} nulo) cuya fecha es anterior o igual a {@code limite}.
     */
    @Modifying
    @Query("DELETE FROM Egreso e WHERE e.reservaId IS NULL AND e.fecha <= :limite")
    int deleteEgresosSueltosConFechaAnteriorA(@Param("limite") LocalDate limite);

}
