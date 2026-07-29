package com.cipolflo.server.shared.email.repository;

import com.cipolflo.server.shared.email.domain.DestinatarioNotificacionEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DestinatarioNotificacionEmailRepository
        extends JpaRepository<DestinatarioNotificacionEmail, Long>,
        JpaSpecificationExecutor<DestinatarioNotificacionEmail> {

    List<DestinatarioNotificacionEmail> findByActivoTrue();
}
