package com.cipolflo.server.servicios.repository;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioSpecificationTest {

    @Mock
    private Root<Servicio> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder cb;

    @Test
    void conNombre_deberiaRetornarConjunctionCuandoNombreEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conNombre(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conNombre_deberiaRetornarConjunctionCuandoNombreEsBlanco() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conNombre("   ");
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conNombre_deberiaCrearPredicateLikeCuandoNombreTieneValor() {
        Expression<String> lowerExpr = mock(Expression.class);
        when(root.get("nombre")).thenReturn(mock(Path.class));
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(any(Expression.class), eq("%cabaña%"))).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conNombre("Cabaña");
        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "%cabaña%");
    }

    @Test
    void conNombre_deberiaTrimearElNombreAntesDeBuscar() {
        Expression<String> lowerExpr = mock(Expression.class);
        when(root.get("nombre")).thenReturn(mock(Path.class));
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(any(Expression.class), eq("%cabaña%"))).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conNombre("  Cabaña  ");
        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "%cabaña%");
    }

    @Test
    void conProcedencia_deberiaRetornarConjunctionCuandoProcedenciaEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conProcedencia(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conProcedencia_deberiaCrearPredicateEqualCuandoProcedenciaTieneValor() {
        Path<Object> path = mock(Path.class);
        when(root.get("procedencia")).thenReturn(path);
        when(cb.equal(path, Procedencia.CAMPING)).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conProcedencia(Procedencia.CAMPING);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, Procedencia.CAMPING);
    }

    @Test
    void conHabilitado_deberiaRetornarConjunctionCuandoHabilitadoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conHabilitado(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conHabilitado_deberiaCrearPredicateEqualParaTrue() {
        Path<Object> path = mock(Path.class);
        when(root.get("habilitado")).thenReturn(path);
        when(cb.equal(path, true)).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conHabilitado(true);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, true);
    }

    @Test
    void conHabilitado_deberiaCrearPredicateEqualParaFalse() {
        Path<Object> path = mock(Path.class);
        when(root.get("habilitado")).thenReturn(path);
        when(cb.equal(path, false)).thenReturn(mock(Predicate.class));

        Specification<Servicio> spec = ServicioSpecification.conHabilitado(false);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, false);
    }
}
