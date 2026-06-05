package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ModificacionParticularRequestDto;
import com.cipolflo.server.clientes.dto.ModificacionSocioRequestDto;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.mapper.ClienteMapper;
import com.cipolflo.server.clientes.repository.ClienteSpecification;
import com.cipolflo.server.clientes.validator.ModificacionParticularValidator;
import com.cipolflo.server.clientes.validator.ModificacionSocioValidator;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteService implements IClienteService {
    private final ClienteRepository clienteRepository;
    private final ModificacionParticularValidator modificacionParticularValidator;
    private final ModificacionSocioValidator modificacionSocioValidator;

    public ClienteService(ClienteRepository clienteRepository,
                          ModificacionParticularValidator modificacionParticularValidator,
                          ModificacionSocioValidator modificacionSocioValidator) {
        this.clienteRepository = clienteRepository;
        this.modificacionParticularValidator = modificacionParticularValidator;
        this.modificacionSocioValidator = modificacionSocioValidator;
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
    @Transactional
    public ClienteResponseDto modificarParticular(Long id, ModificacionParticularRequestDto dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));

        if (!(cliente instanceof Particular particular)) {
            throw new ClienteNotFoundException(id);
        }

        modificacionParticularValidator.validar(id, dto);

        String cedulaNormalizada = dto.getCedula().replaceAll("\\D", "");
        String mailNormalizado = dto.getMail() != null ? dto.getMail().trim() : null;

        particular.modificar(cedulaNormalizada, dto.getNombreCompleto(), dto.getTelefono(), mailNormalizado, dto.getNotas());

        return ClienteMapper.toDetalleResponseDto(clienteRepository.save(particular));
    }

    @Override
    @Transactional
    public ClienteResponseDto modificarSocio(Long id, ModificacionSocioRequestDto dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));

        if (!(cliente instanceof Socio socio)) {
            throw new ClienteNotFoundException(id);
        }

        modificacionSocioValidator.validar(id, dto);

        String cedulaNormalizada = dto.getCedula().replaceAll("\\D", "");
        String mailNormalizado = dto.getMail() != null ? dto.getMail().trim() : null;

        socio.modificar(
                cedulaNormalizada,
                dto.getNombreCompleto(),
                dto.getTelefono(),
                mailNormalizado,
                dto.getNotas(),
                dto.getFechaNacimiento(),
                dto.getPais(),
                dto.getDepartamento(),
                dto.getCiudad(),
                dto.getDireccion(),
                dto.getMetodoCobro()
        );

        return ClienteMapper.toDetalleResponseDto(clienteRepository.save(socio));
    }
}
