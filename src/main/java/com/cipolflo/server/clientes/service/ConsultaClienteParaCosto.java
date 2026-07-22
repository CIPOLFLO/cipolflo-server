package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsultaClienteParaCosto implements IConsultaClienteParaCosto {

    private final ClienteRepository clienteRepository;

    public ConsultaClienteParaCosto(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente obtenerCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
    }
}