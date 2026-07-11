package com.cipolflo.server.clientes.repository;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Empresa;
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

    @Test
    @SuppressWarnings("unchecked")
    void conTipoCliente_deberiaCrearPredicateParaEmpresa() {
        Expression<Class<? extends Cliente>> typeExpr = mock(Expression.class);
        when(root.type()).thenReturn(typeExpr);
        when(cb.equal(typeExpr, Empresa.class)).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conTipoCliente(TipoCliente.EMPRESA);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(typeExpr, Empresa.class);
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
    void conIdentificador_deberiaBuscarPorCedulaNormalizada() {
        Path<Object> cedulaPath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);
        Expression<String> lowerRut = mock(Expression.class);
        Root<Socio> socioRoot = mock(Root.class);
        Path<Object> nroSocioPath = mock(Path.class);
        Expression<String> textExpr = mock(Expression.class);
        Root<Empresa> empresaRoot = mock(Root.class);
        Path<Object> rutPath = mock(Path.class);

        when(root.get("cedula")).thenReturn(cedulaPath);
        // cb.lower se invoca primero para la cédula y luego para el rut
        when(cb.lower(any())).thenReturn(lowerExpr, lowerRut);
        when(cb.like(eq(lowerExpr), eq("12345678%"))).thenReturn(mock(Predicate.class));
        when(cb.like(eq(lowerRut), eq("12345678%"))).thenReturn(mock(Predicate.class));
        doReturn(socioRoot).when(cb).treat(root, Socio.class);
        when(socioRoot.get("numeroSocio")).thenReturn(nroSocioPath);
        when(cb.function("TEXT", String.class, nroSocioPath)).thenReturn(textExpr);
        when(cb.like(eq(textExpr), eq("12345678%"))).thenReturn(mock(Predicate.class));
        doReturn(empresaRoot).when(cb).treat(root, Empresa.class);
        when(empresaRoot.get("rut")).thenReturn(rutPath);
        when(cb.or(any(), any(), any())).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec =
                ClienteSpecification.conIdentificador("1.234.567-8");

        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "12345678%");
    }

    @Test
    @SuppressWarnings("unchecked")
    void conIdentificador_deberiaBuscarPorCedulaParcial() {
        Path<Object> cedulaPath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);
        Expression<String> lowerRut = mock(Expression.class);
        Root<Socio> socioRoot = mock(Root.class);
        Path<Object> nroSocioPath = mock(Path.class);
        Expression<String> textExpr = mock(Expression.class);
        Root<Empresa> empresaRoot = mock(Root.class);
        Path<Object> rutPath = mock(Path.class);

        when(root.get("cedula")).thenReturn(cedulaPath);
        // cb.lower se invoca primero para la cédula y luego para el rut
        when(cb.lower(any())).thenReturn(lowerExpr, lowerRut);
        when(cb.like(eq(lowerExpr), eq("123%"))).thenReturn(mock(Predicate.class));
        when(cb.like(eq(lowerRut), eq("123%"))).thenReturn(mock(Predicate.class));
        doReturn(socioRoot).when(cb).treat(root, Socio.class);
        when(socioRoot.get("numeroSocio")).thenReturn(nroSocioPath);
        when(cb.function("TEXT", String.class, nroSocioPath)).thenReturn(textExpr);
        when(cb.like(eq(textExpr), eq("123%"))).thenReturn(mock(Predicate.class));
        doReturn(empresaRoot).when(cb).treat(root, Empresa.class);
        when(empresaRoot.get("rut")).thenReturn(rutPath);
        when(cb.or(any(), any(), any())).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec =
                ClienteSpecification.conIdentificador("123");

        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerExpr, "123%");
    }

    @Test
    @SuppressWarnings("unchecked")
    void conIdentificador_deberiaBuscarPorRutDeEmpresa() {
        Path<Object> cedulaPath = mock(Path.class);
        Expression<String> lowerCedula = mock(Expression.class);
        Expression<String> lowerRut = mock(Expression.class);
        Root<Socio> socioRoot = mock(Root.class);
        Path<Object> nroSocioPath = mock(Path.class);
        Expression<String> textExpr = mock(Expression.class);
        Root<Empresa> empresaRoot = mock(Root.class);
        Path<Object> rutPath = mock(Path.class);

        when(root.get("cedula")).thenReturn(cedulaPath);
        // cb.lower se invoca primero para la cédula y luego para el rut
        when(cb.lower(any())).thenReturn(lowerCedula, lowerRut);
        when(cb.like(eq(lowerCedula), eq("123%"))).thenReturn(mock(Predicate.class));
        doReturn(socioRoot).when(cb).treat(root, Socio.class);
        when(socioRoot.get("numeroSocio")).thenReturn(nroSocioPath);
        when(cb.function("TEXT", String.class, nroSocioPath)).thenReturn(textExpr);
        when(cb.like(eq(textExpr), eq("123%"))).thenReturn(mock(Predicate.class));
        doReturn(empresaRoot).when(cb).treat(root, Empresa.class);
        when(empresaRoot.get("rut")).thenReturn(rutPath);
        when(cb.like(eq(lowerRut), eq("123%"))).thenReturn(mock(Predicate.class));
        when(cb.or(any(), any(), any())).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador("123");
        spec.toPredicate(root, query, cb);

        verify(cb).like(lowerRut, "123%");
    }

    @Test
    @SuppressWarnings("unchecked")
    void conIdentificador_deberiaCrearPredicateOrEntreCedulaNroSocioYRut() {
        Path<Object> cedulaPath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);
        Root<Socio> socioRoot = mock(Root.class);
        Path<Object> nroSocioPath = mock(Path.class);
        Expression<String> textExpr = mock(Expression.class);
        Root<Empresa> empresaRoot = mock(Root.class);
        Path<Object> rutPath = mock(Path.class);
        Predicate cedulaPredicate = mock(Predicate.class);
        Predicate nroSocioPredicate = mock(Predicate.class);
        Predicate rutPredicate = mock(Predicate.class);

        when(root.get("cedula")).thenReturn(cedulaPath);
        when(cb.lower(any())).thenReturn(lowerExpr);
        doReturn(socioRoot).when(cb).treat(root, Socio.class);
        when(socioRoot.get("numeroSocio")).thenReturn(nroSocioPath);
        when(cb.function("TEXT", String.class, nroSocioPath)).thenReturn(textExpr);
        doReturn(empresaRoot).when(cb).treat(root, Empresa.class);
        when(empresaRoot.get("rut")).thenReturn(rutPath);
        when(cb.like(lowerExpr, "123%")).thenReturn(cedulaPredicate, rutPredicate);
        when(cb.like(textExpr, "123%")).thenReturn(nroSocioPredicate);
        when(cb.or(cedulaPredicate, nroSocioPredicate, rutPredicate)).thenReturn(mock(Predicate.class));

        Specification<Cliente> spec = ClienteSpecification.conIdentificador("123");
        spec.toPredicate(root, query, cb);

        verify(cb).or(cedulaPredicate, nroSocioPredicate, rutPredicate);
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