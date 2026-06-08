package com.cipolflo.server.clientes.repository;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ClienteSpecification {

    private ClienteSpecification() {}

    public static Specification<Cliente> conTipoCliente(TipoCliente tipoCliente) {
        if (tipoCliente == null)
            return (root, query, cb) -> cb.conjunction();
        Class<?> tipo = tipoCliente == TipoCliente.SOCIO ? Socio.class : Particular.class;
        return (root, query, cb) -> cb.equal(root.type(), tipo);
    }

    public static Specification<Cliente> conNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            return (root, query, cb) -> cb.conjunction();
        String patron = "%" + nombre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombreCompleto")), patron);
    }

    // Acepta solo dígitos o formato parcial de cédula: x.xxx.xxx-x
    private static final String PATRON_IDENTIFICADOR = "^\\d+(\\.\\d+)*(-\\d+)?$";

    public static Specification<Cliente> conIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank())
            return (root, query, cb) -> cb.conjunction();

       if(!CedulaNormalizador.esFormatoValido(identificador))
        return (root, query, cb) -> cb.disjunction(); // Si el formato no es válido, no matchea con nada
        String normalizado = CedulaNormalizador.normalizar(identificador.trim());
        String patron = normalizado + "%";

        return (root, query, cb) -> {
            Predicate porCedula = cb.like(cb.lower(root.get("cedula")), patron);
            Predicate porNroSocio = cb.like(
                    cb.function("TEXT", String.class, cb.treat(root, Socio.class).get("numeroSocio")), patron);
            return cb.or(porCedula, porNroSocio);
        };
    }

    public static Specification<Cliente> conEstado(EstadoSocio estado) {
        if (estado == null)
            return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(
                cb.treat(root, Socio.class).get("estado"), estado);
    }
}
