package com.cipolflo.server.clientes.repository;

import com.cipolflo.server.clientes.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    
    boolean existsByCedulaAndIdNot(String cedula, Long id);

    boolean existsByMailIgnoreCaseAndIdNot(String mail, Long id);

    boolean existsByCedula(String cedula);

    // rut es una propiedad de la subclase Empresa (no de Cliente), por lo que la derivación
    // automática de query no puede resolverla; se consulta explícitamente sobre Empresa.
    @Query("SELECT COUNT(e) > 0 FROM Empresa e WHERE e.rut = :rut AND e.id <> :id")
    boolean existsByRutAndIdNot(@Param("rut") String rut, @Param("id") Long id);

    @Query("SELECT COUNT(e) > 0 FROM Empresa e WHERE e.rut = :rut")
    boolean existsByRut(@Param("rut") String rut);
   
    @Query("SELECT e FROM Empresa e WHERE e.rut = :rut")
    Optional<Cliente> findByRut(@Param("rut") String rut);
    
    boolean existsByMailIgnoreCase(String mail);

    Optional<Cliente> findByCedula(String cedula);

    @Query("SELECT MAX(s.numeroSocio) FROM Socio s")
    Optional<Integer> findMaxNumeroSocio();

}
