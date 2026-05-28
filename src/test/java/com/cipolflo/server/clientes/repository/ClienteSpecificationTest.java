package com.cipolflo.server.clientes.repository;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteSpecificationTest {

    @Mock
    private Root<Cliente> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder cb;

    // --- conTipoCliente ---

    @Test
    void conTipoCliente_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conTipoCliente(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    @SuppressWarnings("unchecked")
    void conTipoCliente_deberiaCrearPredicateParaSocio() {
        Expression<Class<? extends Cliente>> typeExpr = mock(Expression.class);
        when(root.type()).thenReturn(typeExpr);
        when(cb.equal(typeExpr, Socio.class)).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conTipoCliente(TipoCliente.SOCIO);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(typeExpr, Socio.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void conTipoCliente_deberiaCrearPredicateParaParticular() {
        Expression<Class<? extends Cliente>> typeExpr = mock(Expression.class);
        when(root.type()).thenReturn(typeExpr);
        when(cb.equal(typeExpr, Particular.class)).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conTipoCliente(TipoCliente.PARTICULAR);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(typeExpr, Particular.class);
    }

    // --- conNombre ---

    @Test
    void conNombre_deberiaRetornarConjunctionCuandoNombreEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conNombre(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conNombre_deberiaRetornarConjunctionCuandoNombreEsBlanco() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conNombre("   ");
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conNombre_deberiaConvertirAMinusculasAntesDeBuscar() {
        Expression<String> lowerExpr = mock(Expression.class);
        when(root.get("nombreCompleto")).thenReturn(mock(Path.class));
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(any(Expression.class), eq("%juan pérez%"))).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conNombre("JUAN PÉREZ");
        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "%juan pérez%");
    }

    @Test
    void conNombre_deberiaCrearPredicateLikeCuandoNombreTieneValor() {
        Expression<String> lowerExpr = mock(Expression.class);
        when(root.get("nombreCompleto")).thenReturn(mock(Path.class));
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(any(Expression.class), eq("%juan%"))).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conNombre("Juan");
        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "%juan%");
    }

    @Test
    void conNombre_deberiaTrimearElNombreAntesDeBuscar() {
        Expression<String> lowerExpr = mock(Expression.class);
        when(root.get("nombreCompleto")).thenReturn(mock(Path.class));
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(any(Expression.class), eq("%juan%"))).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conNombre("  Juan  ");
        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "%juan%");
    }

    // --- conIdentificador ---

    @Test
    void conIdentificador_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conIdentificador_deberiaRetornarConjunctionCuandoEsBlanco() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador("   ");
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conIdentificador_deberiaRetornarDisjunctionCuandoFormatoEsInvalido() {
        when(cb.disjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador(".-.");
        spec.toPredicate(root, query, cb);

        verify(cb).disjunction();
    }

    @Test
    void conIdentificador_deberiaRetornarDisjunctionCuandoContieneLetras() {
        when(cb.disjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador("abc");
        spec.toPredicate(root, query, cb);

        verify(cb).disjunction();
    }

    @Test
    void conIdentificador_deberiaRetornarDisjunctionCuandoGuionFinalSinNumero() {
        when(cb.disjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador("1.234.567-");
        spec.toPredicate(root, query, cb);

        verify(cb).disjunction();
    }

    @Test
    @SuppressWarnings("unchecked")
    void conIdentificador_deberiaAceptarFormatoParcialConUnPunto() {
        // "1.2" es formato parcial válido
        Path<Object> cedulaPath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);
        Root<Socio> socioRoot = mock(Root.class);
        Path<Object> nroSocioPath = mock(Path.class);
        Expression<String> textExpr = mock(Expression.class);

        when(root.get("cedula")).thenReturn(cedulaPath);
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(eq(lowerExpr), eq("12%"))).thenReturn(mock(Predicate.class));
        doReturn(socioRoot).when(cb).treat(root, Socio.class);
        when(socioRoot.get("numeroSocio")).thenReturn(nroSocioPath);
        when(cb.function("TEXT", String.class, nroSocioPath)).thenReturn(textExpr);
        when(cb.like(eq(textExpr), eq("12%"))).thenReturn(mock(Predicate.class));
        when(cb.or(any(), any())).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador("1.2");
        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "12%");
    }

    @Test
    @SuppressWarnings("unchecked")
    void conIdentificador_deberiaCrearPredicateOrEntreCedulaYNroSocio() {
        Path<Object> cedulaPath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);
        Root<Socio> socioRoot = mock(Root.class);
        Path<Object> nroSocioPath = mock(Path.class);
        Expression<String> textExpr = mock(Expression.class);
        Predicate cedulaPredicate = mock(Predicate.class);
        Predicate nroSocioPredicate = mock(Predicate.class);

        when(root.get("cedula")).thenReturn(cedulaPath);
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(lowerExpr, "123%")).thenReturn(cedulaPredicate);
        doReturn(socioRoot).when(cb).treat(root, Socio.class);
        when(socioRoot.get("numeroSocio")).thenReturn(nroSocioPath);
        when(cb.function("TEXT", String.class, nroSocioPath)).thenReturn(textExpr);
        when(cb.like(textExpr, "123%")).thenReturn(nroSocioPredicate);
        when(cb.or(cedulaPredicate, nroSocioPredicate)).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador("123");
        spec.toPredicate(root, query, cb);

        verify(cb).or(cedulaPredicate, nroSocioPredicate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void conIdentificador_deberiaNormalizarCedulaQuitandoPuntosYGuiones() {
        Path<Object> cedulaPath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);
        Root<Socio> socioRoot = mock(Root.class);
        Path<Object> nroSocioPath = mock(Path.class);
        Expression<String> textExpr = mock(Expression.class);

        when(root.get("cedula")).thenReturn(cedulaPath);
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(eq(lowerExpr), eq("12345678%"))).thenReturn(mock(Predicate.class));
        doReturn(socioRoot).when(cb).treat(root, Socio.class);
        when(socioRoot.get("numeroSocio")).thenReturn(nroSocioPath);
        when(cb.function("TEXT", String.class, nroSocioPath)).thenReturn(textExpr);
        when(cb.like(eq(textExpr), eq("12345678%"))).thenReturn(mock(Predicate.class));
        when(cb.or(any(), any())).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador("1.234.567-8");
        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "12345678%");
    }

    // --- conEstado ---

    @Test
    void conEstado_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conEstado(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    @SuppressWarnings("unchecked")
    void conEstado_deberiaCrearPredicateEqualCuandoEstadoTieneValor() {
        Root<Socio> socioRoot = mock(Root.class);
        Path<Object> estadoPath = mock(Path.class);

        doReturn(socioRoot).when(cb).treat(root, Socio.class);
        when(socioRoot.get("estado")).thenReturn(estadoPath);
        when(cb.equal(estadoPath, EstadoSocio.ACTIVO)).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conEstado(EstadoSocio.ACTIVO);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(estadoPath, EstadoSocio.ACTIVO);
    }
}
