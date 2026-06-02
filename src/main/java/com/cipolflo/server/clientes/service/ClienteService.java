package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.mapper.ClienteMapper;
import com.cipolflo.server.clientes.repository.ClienteSpecification;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteService implements IClienteService {
    private final ClienteRepository clienteRepository;
    private final IReservaService reservaService;

    public ClienteService(ClienteRepository clienteRepository, IReservaService reservaService) {
        this.clienteRepository = clienteRepository;
        this.reservaService = reservaService;
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

    @Override
    public Map<Long, String> getNombresByIds(Collection<Long> ids) {
        return clienteRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        Cliente::getId,
                        Cliente::getNombreCompleto
                ));
    }

    @Override
    public void darDeBajaSocio(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new SocioNotFoundException(id));
        if (!(cliente instanceof Socio socio)) {
            throw new SocioNotFoundException(id);
        }
        socio.darDeBaja();
        reservaService.cancelarReservasFuturasPorCliente(socio.getId());
        clienteRepository.save(socio);
    }


}
