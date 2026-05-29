package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Map<Long, String> getNombresByIds(Collection<Long> ids) {
        return clienteRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        Cliente::getId,
                        Cliente::getNombreCompleto
                ));
    }
}
