package com.cipolflo.server.shared.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface EnvioEmailLogRepository extends JpaRepository<EnvioEmailLog, Long> {

    /**
     * Purga registros anteriores a {@code limite} en un único {@code DELETE} masivo.
     * Usado por el scheduler de retención. Se usa {@code @Query} explícito (y no un
     * derivado {@code deleteBy…}) para evitar que Spring Data cargue las entidades y las
     * borre una a una. Devuelve la cantidad de filas eliminadas.
     */
    @Modifying
    @Query("delete from EnvioEmailLog e where e.createdAt < :limite")
    int deleteByCreatedAtBefore(@Param("limite") Instant limite);
}
