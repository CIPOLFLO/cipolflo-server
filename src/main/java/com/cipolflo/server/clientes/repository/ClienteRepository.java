package com.cipolflo.server.clientes.repository;

import com.cipolflo.server.clientes.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
