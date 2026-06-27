package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.mapper.ClienteMapper;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.ClienteSpecification;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultaClienteDetalle implements IConsultaClienteDetalle {
    private final ClienteRepository clienteRepository;

    public ConsultaClienteDetalle(
            ClienteRepository clienteRepository
    ){
        this.clienteRepository = clienteRepository;
    }

    @Override
    public ClienteDetalleReservaDto getDetallClienteSimple(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
        return ClienteMapper.toClienteDetalleReservaDto(cliente);
    }

    @Override
    public List<Long> getIdsByNombre(String nombre) {
        return clienteRepository.findAll(ClienteSpecification.conNombre(nombre))
                .stream().map(Cliente::getId).toList();
    }

    @Override
    public Map<Long, String> getNombresByIds(Collection<Long> ids) {
        return clienteRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Cliente::getId, Cliente::getNombreCompleto));
    }
}
