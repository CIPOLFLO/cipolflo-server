package com.cipolflo.server.reservas.repository;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ReservaSpecificationTest {

    @Mock private Root<Reserva> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;

    // ── conProcedencia ─────────────────────────────────────────────────────────

    @Test
    void conProcedencia_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conProcedencia(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conProcedencia_deberiaCrearPredicateEqualCuandoTieneValor() {
        Path<Object> path = mock(Path.class);
        when(root.get("procedencia")).thenReturn(path);
        when(cb.equal(path, Procedencia.CAMPING)).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conProcedencia(Procedencia.CAMPING);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, Procedencia.CAMPING);
    }

    // ── conServicioId ──────────────────────────────────────────────────────────

    @Test
    void conServicioId_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conServicioId(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conServicioId_deberiaCrearPredicateEqualCuandoTieneValor() {
        Path<Object> path = mock(Path.class);
        when(root.get("servicioId")).thenReturn(path);
        when(cb.equal(path, 5L)).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conServicioId(5L);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, 5L);
    }

    // ── conEstado ──────────────────────────────────────────────────────────────

    @Test
    void conEstado_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conEstado(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conEstado_deberiaCrearPredicateEqualCuandoTieneValor() {
        Path<Object> path = mock(Path.class);
        when(root.get("estado")).thenReturn(path);
        when(cb.equal(path, EstadoReserva.PENDIENTE)).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conEstado(EstadoReserva.PENDIENTE);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, EstadoReserva.PENDIENTE);
    }

    // ── conFechaEntradaDesde ───────────────────────────────────────────────────

    @Test
    void conFechaEntradaDesde_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conFechaEntradaDesde(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conFechaEntradaDesde_deberiaCrearPredicateGreaterThanOrEqualTo() {
        LocalDate fecha = LocalDate.of(2026, 7, 1);
        Path<LocalDate> path = mock(Path.class);
        doReturn(path).when(root).get("fechaEntrada");
        when(cb.greaterThanOrEqualTo(path, fecha)).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conFechaEntradaDesde(fecha);
        spec.toPredicate(root, query, cb);

        verify(cb).greaterThanOrEqualTo(path, fecha);
    }

    // ── conFechaSalidaHasta ────────────────────────────────────────────────────

    @Test
    void conFechaSalidaHasta_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conFechaSalidaHasta(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conFechaSalidaHasta_deberiaCrearPredicateLessThanOrEqualTo() {
        LocalDate fecha = LocalDate.of(2026, 7, 31);
        Path<LocalDate> path = mock(Path.class);
        doReturn(path).when(root).get("fechaSalida");
        when(cb.lessThanOrEqualTo(path, fecha)).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conFechaSalidaHasta(fecha);
        spec.toPredicate(root, query, cb);

        verify(cb).lessThanOrEqualTo(path, fecha);
    }

    // ── conClienteIds ──────────────────────────────────────────────────────────

    @Test
    void conClienteIds_deberiaRetornarConjunctionCuandoEsNull() {
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conClienteIds(null);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
    }

    @Test
    void conClienteIds_deberiaCrearPredicateInCuandoTieneValores() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Path<Object> path = mock(Path.class);
        when(root.get("clienteId")).thenReturn(path);
        when(path.in(ids)).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.conClienteIds(ids);
        spec.toPredicate(root, query, cb);

        verify(path).in(ids);
    }

    // ── ordenadoPorNombreCliente ───────────────────────────────────────────────

    @Test
    void ordenadoPorNombreCliente_deberiaRetornarConjunctionEnQueryDeConteo() {
        doReturn(Long.class).when(query).getResultType();
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.ordenadoPorNombreCliente(Sort.Direction.ASC);
        spec.toPredicate(root, query, cb);

        verify(cb).conjunction();
        verify(query, never()).from(any(Class.class));
    }

    @Test
    void ordenadoPorNombreCliente_deberiaAgregarJoinYOrdenAscEnQueryPrincipal() {
        Root<Cliente> clienteRoot = mock(Root.class);
        Path<Object> clienteIdPath = mock(Path.class);
        Path<Object> idPath = mock(Path.class);
        Path<Object> nombrePath = mock(Path.class);
        Order order = mock(Order.class);

        doReturn(Reserva.class).when(query).getResultType();
        doReturn(clienteRoot).when(query).from(Cliente.class);
        doReturn(nombrePath).when(clienteRoot).get("nombreCompleto");
        when(cb.asc(nombrePath)).thenReturn(order);
        when(root.get("clienteId")).thenReturn(clienteIdPath);
        doReturn(idPath).when(clienteRoot).get("id");
        when(cb.equal(clienteIdPath, idPath)).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.ordenadoPorNombreCliente(Sort.Direction.ASC);
        spec.toPredicate(root, query, cb);

        verify(query).from(Cliente.class);
        verify(cb).asc(nombrePath);
        verify(query).orderBy(order);
    }

    @Test
    void ordenadoPorNombreCliente_deberiaAgregarOrdenDescEnQueryPrincipal() {
        Root<Cliente> clienteRoot = mock(Root.class);
        Path<Object> clienteIdPath = mock(Path.class);
        Path<Object> idPath = mock(Path.class);
        Path<Object> nombrePath = mock(Path.class);
        Order order = mock(Order.class);

        doReturn(Reserva.class).when(query).getResultType();
        doReturn(clienteRoot).when(query).from(Cliente.class);
        doReturn(nombrePath).when(clienteRoot).get("nombreCompleto");
        when(cb.desc(nombrePath)).thenReturn(order);
        when(root.get("clienteId")).thenReturn(clienteIdPath);
        doReturn(idPath).when(clienteRoot).get("id");
        when(cb.equal(clienteIdPath, idPath)).thenReturn(mock(Predicate.class));

        Specification<Reserva> spec = ReservaSpecification.ordenadoPorNombreCliente(Sort.Direction.DESC);
        spec.toPredicate(root, query, cb);

        verify(query).from(Cliente.class);
        verify(cb).desc(nombrePath);
        verify(query).orderBy(order);
    }
}
