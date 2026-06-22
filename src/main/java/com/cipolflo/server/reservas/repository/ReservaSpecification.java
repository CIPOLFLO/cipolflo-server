package com.cipolflo.server.reservas.repository;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collection;

public class ReservaSpecification {

    private ReservaSpecification() {}

    public static Specification<Reserva> conProcedencia(Procedencia procedencia) {
        if (procedencia == null) return (r, q, cb) -> cb.conjunction();
        return (r, q, cb) -> cb.equal(r.get("procedencia"), procedencia);
    }

    public static Specification<Reserva> conServicioId(Long servicioId) {
        if (servicioId == null) return (r, q, cb) -> cb.conjunction();
        return (r, q, cb) -> cb.equal(r.get("servicioId"), servicioId);
    }

    public static Specification<Reserva> conEstado(EstadoReserva estado) {
        if (estado == null) return (r, q, cb) -> cb.conjunction();
        return (r, q, cb) -> cb.equal(r.get("estado"), estado);
    }

    public static Specification<Reserva> conFechaEntradaDesde(LocalDate fecha) {
        if (fecha == null) return (r, q, cb) -> cb.conjunction();
        return (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("fechaEntrada"), fecha);
    }

    public static Specification<Reserva> conFechaSalidaHasta(LocalDate fecha) {
        if (fecha == null) return (r, q, cb) -> cb.conjunction();
        return (r, q, cb) -> cb.lessThanOrEqualTo(r.get("fechaSalida"), fecha);
    }

    public static Specification<Reserva> conClienteIds(Collection<Long> ids) {
        if (ids == null) return (r, q, cb) -> cb.conjunction();
        return (r, q, cb) -> r.get("clienteId").in(ids);
    }

    /**
     * Agrega un cross join a Cliente para ordenar por nombre. Solo aplica a la query principal,
     * no a la count query, para evitar que el conteo excluya reservas sin cliente.
     */
    public static Specification<Reserva> ordenadoPorNombreCliente(Sort.Direction direction) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                Root<Cliente> clienteRoot = query.from(Cliente.class);
                Order order = direction == Sort.Direction.ASC
                        ? cb.asc(clienteRoot.get("nombreCompleto"))
                        : cb.desc(clienteRoot.get("nombreCompleto"));
                query.orderBy(order);
                return cb.equal(root.get("clienteId"), clienteRoot.get("id"));
            }
            return cb.conjunction();
        };
    }
}
