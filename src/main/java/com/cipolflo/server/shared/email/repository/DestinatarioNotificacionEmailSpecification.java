package com.cipolflo.server.shared.email.repository;

import com.cipolflo.server.shared.email.domain.DestinatarioNotificacionEmail;
import org.springframework.data.jpa.domain.Specification;

public class DestinatarioNotificacionEmailSpecification {

    private DestinatarioNotificacionEmailSpecification() {}

    public static Specification<DestinatarioNotificacionEmail> conAlias(String alias) {
        if (alias == null || alias.isBlank())
            return (root, query, cb) -> cb.conjunction();
        String patron = "%" + alias.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("alias")), patron);
    }

    public static Specification<DestinatarioNotificacionEmail> conActivo(Boolean activo) {
        if (activo == null)
            return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }
}
