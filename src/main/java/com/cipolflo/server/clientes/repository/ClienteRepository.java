package com.cipolflo.server.clientes.repository;

import com.cipolflo.server.clientes.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    // TODO DEV-74: reemplazar por el método del servicio de búsqueda de clientes por cédula cuando esté implementado
    boolean existsByCedulaAndIdNot(String cedula, Long id);

    boolean existsByMailIgnoreCaseAndIdNot(String mail, Long id);

    boolean existsByCedula(String cedula);

    boolean existsByMailIgnoreCase(String mail);

    Optional<Cliente> findByCedula(String cedula);

    @Query("SELECT MAX(s.numeroSocio) FROM Socio s")
    Optional<Integer> findMaxNumeroSocio();

}
