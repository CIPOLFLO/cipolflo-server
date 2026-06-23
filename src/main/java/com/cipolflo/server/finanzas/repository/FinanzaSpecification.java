package com.cipolflo.server.finanzas.repository;

import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class FinanzaSpecification {

    private FinanzaSpecification() {
    }

    public static Specification<Finanza> conFechaDesde(LocalDate fechaDesde) {
        return (root, query, cb) -> fechaDesde == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde);
    }

    public static Specification<Finanza> conFechaHasta(LocalDate fechaHasta) {
        return (root, query, cb) -> fechaHasta == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta);
    }

    public static Specification<Finanza> conConcepto(Concepto concepto) {
        return (root, query, cb) -> concepto == null
                ? cb.conjunction()
                : cb.equal(root.get("concepto"), concepto);
    }

    public static Specification<Finanza> conTipoMovimiento(TipoMovimiento tipoMovimiento) {
        return (root, query, cb) -> {
            if (tipoMovimiento == null) {
                return cb.conjunction();
            }

            Class<?> tipoClase = tipoMovimiento == TipoMovimiento.INGRESO
                    ? Ingreso.class
                    : Egreso.class;

            return cb.equal(root.type(), tipoClase);
        };
    }

    public static Specification<Finanza> desdeFiltros(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Concepto concepto,
            TipoMovimiento tipoMovimiento
    ) {
        return conFechaDesde(fechaDesde)
                .and(conFechaHasta(fechaHasta))
                .and(conConcepto(concepto))
                .and(conTipoMovimiento(tipoMovimiento));
    }
}