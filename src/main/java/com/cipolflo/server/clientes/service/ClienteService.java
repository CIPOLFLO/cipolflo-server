package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.mapper.ClienteMapper;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.ClienteSpecification;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ClienteService implements IClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public PageResponse<ListadoClientesResponseDto> getListadoClientes(ListadoClientesRequestDto filtros, PageRequestDto pageRequest) {
        Specification<Cliente> spec = ClienteSpecification
                .conEstado(filtros.estado())
                .and(ClienteSpecification.conNombre(filtros.nombre()))
                .and(ClienteSpecification.conTipoCliente(filtros.tipoCliente()))
                .and(ClienteSpecification.conIdentificador(filtros.identificador()));

        Page<ListadoClientesResponseDto> page = clienteRepository
                .findAll(spec, pageRequest.toPageable())
                .map(ClienteMapper::toListadoResponseDto);

        return PaginationMapper.toPageResponse(page);
    }

    @Override
    public ClienteResponseDto getDetalleCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
        return ClienteMapper.toDetalleResponseDto(cliente);
    }
}
