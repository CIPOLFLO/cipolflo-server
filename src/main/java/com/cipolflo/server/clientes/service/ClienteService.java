package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.PagoCuota;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.dto.*;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.mapper.ClienteMapper;
import com.cipolflo.server.clientes.repository.ClienteSpecification;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.repository.PagoCuotaRepository;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import com.cipolflo.server.clientes.validator.ModificacionParticularValidator;
import com.cipolflo.server.clientes.validator.ModificacionSocioValidator;
import com.cipolflo.server.clientes.validator.RegistroSocioValidator;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import com.cipolflo.server.clientes.service.PagoCuotaService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteService implements IClienteService {
    private final ClienteRepository clienteRepository;
    private final IReservaService reservaService;
    private final ModificacionParticularValidator modificacionParticularValidator;
    private final ModificacionSocioValidator modificacionSocioValidator;
    private final RegistroSocioValidator registroSocioValidator;
    private final PagoCuotaRepository pagoCuotaRepository;
    private final PagoCuotaService pagoCuotaService;

    public ClienteService(ClienteRepository clienteRepository,
                          IReservaService reservaService,
                          PagoCuotaRepository pagoCuotaRepository,
                          ModificacionParticularValidator modificacionParticularValidator,
                          ModificacionSocioValidator modificacionSocioValidator,
                          RegistroSocioValidator registroSocioValidator,
                          PagoCuotaService pagoCuotaService) {
        this.clienteRepository = clienteRepository;
        this.reservaService = reservaService;
        this.modificacionParticularValidator = modificacionParticularValidator;
        this.modificacionSocioValidator = modificacionSocioValidator;
        this.registroSocioValidator = registroSocioValidator;
        this.pagoCuotaRepository = pagoCuotaRepository;
        this.pagoCuotaService = pagoCuotaService;
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
        PagoCuota ultimaCuotaPaga = cliente instanceof Socio socio
                ? pagoCuotaRepository.findTopBySocioIdOrderByFechaDesc(socio.getId()).orElse(null)
                : null;
        String mesCorrespondiente = ultimaCuotaPaga != null
                ? pagoCuotaService.obtenerMesCorrespondiente(ultimaCuotaPaga)
                : null;
        return ClienteMapper.toDetalleResponseDto(clienteRepository.saveAndFlush(cliente), ultimaCuotaPaga, mesCorrespondiente);
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

    @Override
    @Transactional
    public ClienteResponseDto modificarParticular(Long id, ModificacionParticularRequestDto dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));

        if (!(cliente instanceof Particular particular)) {
            throw new ClienteNotFoundException(id);
        }

        String cedulaNormalizada = CedulaNormalizador.normalizar(dto.getCedula());
        String mailNormalizado = dto.getMail() != null ? dto.getMail().trim() : null;
        modificacionParticularValidator.validar(id, dto, cedulaNormalizada, mailNormalizado);

        particular.modificar(cedulaNormalizada, dto.getNombreCompleto(), dto.getTelefono(), mailNormalizado, dto.getNotas());

        try {
            return ClienteMapper.toDetalleResponseDto(clienteRepository.saveAndFlush(particular), null, null);
        } catch (DataIntegrityViolationException e) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.CEDULA_DUPLICADA.name(),
                    "Ya existe un cliente con esa cédula"
            );
        }
    }

    @Override
    @Transactional
    public ClienteResponseDto modificarSocio(Long id, ModificacionSocioRequestDto dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));

        if (!(cliente instanceof Socio socio)) {
            throw new ClienteNotFoundException(id);
        }

        String cedulaNormalizada = CedulaNormalizador.normalizar(dto.getCedula());
        String mailNormalizado = dto.getMail() != null ? dto.getMail().trim() : null;
        modificacionSocioValidator.validar(id, dto, cedulaNormalizada, mailNormalizado);

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
        PagoCuota ultimaCuotaPaga = pagoCuotaRepository
                .findTopBySocioIdOrderByFechaDesc(socio.getId())
                .orElse(null);

        String mesCorrespondiente = ultimaCuotaPaga != null
                ? pagoCuotaService.obtenerMesCorrespondiente(ultimaCuotaPaga)
                : null;

        try {
            return ClienteMapper.toDetalleResponseDto(clienteRepository.saveAndFlush(socio), ultimaCuotaPaga,mesCorrespondiente);
        } catch (DataIntegrityViolationException e) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.CEDULA_DUPLICADA.name(),
                    "Ya existe un cliente con esa cédula"
            );
        }
    }

    @Override
    @Transactional
    public ClienteResponseDto registrarSocio(RegistroSocioRequestDto dto) {
        String cedulaNormalizada = CedulaNormalizador.normalizar(dto.getCedula());
        String mailNormalizado = dto.getEmail() != null ? dto.getEmail().trim() : null;
        registroSocioValidator.validar(dto, cedulaNormalizada, mailNormalizado);
        // TODO: definir estrategia de asignación de número de socio (secuencia DB, lock pesimista, etc.)
        Integer numeroSocio = clienteRepository.findMaxNumeroSocio().orElse(0) + 1;
        Socio socio = new Socio();
        socio.setCedula(cedulaNormalizada);
        socio.setNombreCompleto(dto.getNombreCompleto());
        socio.setTelefono(dto.getTelefono());
        socio.setMail(mailNormalizado);
        socio.setFechaNacimiento(dto.getFechaNacimiento());
        socio.setMetodoCobro(dto.getMetodoCobro());
        socio.setPais(dto.getPais());
        socio.setDepartamento(dto.getDepartamento());
        socio.setCiudad(dto.getCiudad());
        socio.setDireccion(dto.getDireccion());
        socio.setNotas(dto.getObservaciones());
        socio.setNumeroSocio(numeroSocio);
        socio.setEstado(EstadoSocio.ACTIVO);
        socio.setFechaIngreso(LocalDate.now(ZoneId.systemDefault()));
        socio.setMesesSinPagar(0);
        return ClienteMapper.toDetalleResponseDto(clienteRepository.save(socio), null,null);
    }
}
