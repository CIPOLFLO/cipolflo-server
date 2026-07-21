package com.cipolflo.server.integraciones.telegram.repository;

import com.cipolflo.server.integraciones.telegram.domain.TelegramChatAutorizado;
import org.springframework.data.jpa.domain.Specification;

public class TelegramChatAutorizadoSpecification {

    private TelegramChatAutorizadoSpecification() {}

    public static Specification<TelegramChatAutorizado> conAlias(String alias) {
        if (alias == null || alias.isBlank())
            return (root, query, cb) -> cb.conjunction();
        String patron = "%" + alias.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("alias")), patron);
    }

    public static Specification<TelegramChatAutorizado> conActivo(Boolean activo) {
        if (activo == null)
            return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }
}
