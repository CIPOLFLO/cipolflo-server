package com.cipolflo.server.servicios.repository;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.shared.enums.Procedencia;
import org.springframework.data.jpa.domain.Specification;

public class ServicioSpecification {

    private ServicioSpecification() {}

    public static Specification<Servicio> conNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            return (root, query, cb) -> cb.conjunction();
        String patron = "%" + nombre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron);
    }

    public static Specification<Servicio> conProcedencia(Procedencia procedencia) {
        if (procedencia == null)
            return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("procedencia"), procedencia);
    }

    public static Specification<Servicio> conHabilitado(Boolean habilitado) {
        if (habilitado == null)
            return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("habilitado"), habilitado);
    }
}
