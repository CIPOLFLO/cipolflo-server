package com.cipolflo.server.shared.email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface EnvioEmailLogRepository extends JpaRepository<EnvioEmailLog, Long> {

    /** Purga registros anteriores a {@code limite}. Usado por el scheduler de retención. */
    long deleteByCreatedAtBefore(Instant limite);
}
