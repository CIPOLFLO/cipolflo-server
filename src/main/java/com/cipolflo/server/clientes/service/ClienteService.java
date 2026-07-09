package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Empresa;
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
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import com.cipolflo.server.clientes.utils.RutNormalizador;
import com.cipolflo.server.clientes.validator.*;
import com.cipolflo.server.shared.export.*;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteService implements IClienteService {
    private final ClienteRepository clienteRepository;
    private final IReservaService reservaService;
    private final ModificacionParticularValidator modificacionParticularValidator;
    private final ModificacionSocioValidator modificacionSocioValidator;
    private final RegistroSocioValidator registroSocioValidator;
    private final CedulaFormatoValidator cedulaFormatoValidator;
    private final RegistroParticularValidator registroParticularValidator;
    private final RegistroEmpresaValidator registroEmpresaValidator;
    private final ExportProperties exportProperties;
    private final IExportService exportService;
    private final IPagoCuotaService pagoCuotaService;

    public ClienteService(ClienteRepository clienteRepository,
                          IReservaService reservaService,
                          ModificacionParticularValidator modificacionParticularValidator,
                          CedulaFormatoValidator cedulaFormatoValidator,
                          ModificacionSocioValidator modificacionSocioValidator,
                          RegistroSocioValidator registroSocioValidator,
                          RegistroParticularValidator registroParticularValidator,
                          RegistroEmpresaValidator registroEmpresaValidator,
                          ExportProperties exportProperties,
                          IExportService exportService,
                          IPagoCuotaService pagoCuotaService) {
        this.clienteRepository = clienteRepository;
        this.reservaService = reservaService;
        this.modificacionParticularValidator = modificacionParticularValidator;
        this.modificacionSocioValidator = modificacionSocioValidator;
        this.registroSocioValidator = registroSocioValidator;
        this.cedulaFormatoValidator = cedulaFormatoValidator;
        this.registroParticularValidator = registroParticularValidator;
        this.registroEmpresaValidator = registroEmpresaValidator;
        this.exportProperties = exportProperties;
        this.exportService = exportService;
        this.pagoCuotaService = pagoCuotaService;
    }

    @Override
    public PageResponse<ListadoClientesResponseDto> getListadoClientes(
            ListadoClientesRequestDto filtros,
            PageRequestDto pageRequest
    ) {
        Specification<Cliente> spec = ClienteSpecification
                .conEstado(filtros.estado())
                .and(ClienteSpecification.conNombre(filtros.nombre()))
                .and(ClienteSpecification.conTipoCliente(filtros.tipoCliente()))
                .and(ClienteSpecification.conIdentificador(filtros.identificador()));
        Page<ListadoClientesResponseDto> page = clienteRepository
                .findAll(spec, pageRequest.toPageable())
                .map(cliente -> {
                    UltimaCuotaDto ultimaCuotaPaga = cliente instanceof Socio
                            ? pagoCuotaService.calcularUltimaCuotaPaga(cliente.getId())
                            : null;
                    return ClienteMapper.toListadoResponseDto(cliente, ultimaCuotaPaga);
                });
        return PaginationMapper.toPageResponse(page);
    }

    @Override
    public ClienteResponseDto getDetalleCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
        UltimaCuotaDto ultimaCuotaPagaDto = cliente instanceof Socio
                ? pagoCuotaService.calcularUltimaCuotaPaga(cliente.getId())
                : null;
        return ClienteMapper.toDetalleResponseDto(cliente, ultimaCuotaPagaDto);
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
            return ClienteMapper.toDetalleResponseDto(clienteRepository.saveAndFlush(particular), null);
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

        try {
            return ClienteMapper.toDetalleResponseDto(clienteRepository.saveAndFlush(socio), null);
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
        try {
            return ClienteMapper.toDetalleResponseDto(clienteRepository.saveAndFlush(socio), null);
        } catch (DataIntegrityViolationException e) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.CEDULA_DUPLICADA.name(),
                    "Ya existe un cliente con esa cédula"
            );
        }
    }

    @Override
    @Transactional
    public ClienteResponseDto registrarEmpresa(RegistroEmpresaRequestDto dto) {
        String rutNormalizado = RutNormalizador.normalizar(dto.getRut());
        String mailNormalizado = dto.getMail() != null ? dto.getMail().trim() : null;
        registroEmpresaValidator.validar(dto, rutNormalizado, mailNormalizado);
        Empresa empresa = Empresa.registrar(
                rutNormalizado,
                dto.getRazonSocial(),
                dto.getTelefono(),
                mailNormalizado,
                dto.getPais(),
                dto.getDepartamento(),
                dto.getCiudad(),
                dto.getDireccion(),
                dto.getObservaciones()
        );
        try {
            return ClienteMapper.toDetalleResponseDto(clienteRepository.saveAndFlush(empresa), null);
        } catch (DataIntegrityViolationException e) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.RUT_DUPLICADO.name(),
                    "Ya existe un cliente con ese RUT"
            );
        }
    }

    @Override
    public BusquedaCedulaResponseDto buscarPorCedula(String cedula) {

        cedulaFormatoValidator.validar(cedula);

        String cedulaNormalizada =
                CedulaNormalizador.normalizar(cedula);

        return clienteRepository.findByCedula(cedulaNormalizada)
                .map(ClienteMapper::toBusquedaCedulaResponseDto)
                .orElseThrow(() ->
                        new ClienteNotFoundException(
                                "No existe un cliente con esa cédula"
                        ));
    }

    @Override
    public EstadoSocioResponseDto consultarEstadoSocio(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new SocioNotFoundException(id));
        if (!(cliente instanceof Socio socio)) {
            throw new SocioNotFoundException(id);
        }
        return ClienteMapper.toEstadoSocioResponseDto(socio);
    }

    @Override
    public ArchivoExportado exportarClientes(ListadoClientesRequestDto filtros){
        Specification<Cliente> spec = ClienteSpecification
                .conEstado(filtros.estado())
                .and(ClienteSpecification.conNombre(filtros.nombre()))
                .and(ClienteSpecification.conTipoCliente(filtros.tipoCliente()))
                .and(ClienteSpecification.conIdentificador(filtros.identificador()));

        List<Cliente> clientes = clienteRepository.findAll(spec);

        if(clientes.isEmpty()){
            throw new ExportacionException("No hay registros que coincidan con los filtros aplicados");
        }

        if (clientes.size() > exportProperties.maxFilas()) {
            throw new ExportacionException(
                    "La exportación supera el límite de " + exportProperties.maxFilas() + " filas"
            );
        }

        List<String> encabezados = List.of(
                "Nombre",
                "Número de socio",
                "Cédula",
                "RUT",
                "Email",
                "Estado",
                "Telefono",
                "Notas",
                "Método de cobro",
                "País",
                "Departamento",
                "Dirección",
                "Fecha ingreso",
                "Fecha último pago");

        List<List<String>> filas = clientes.stream()
            .map(ClienteMapper::toExportFila)
            .toList();

        int[] anchos = {8000,5000,5000,5000,10000,5000,5000,5000,5000,5000,5000,5000,5000,5000};
        byte[] contenido = exportService.generarExcel(
                "Clientes",
                encabezados,
                filas,
                anchos
        );

        String nombre = NombreArchivoExport.generar("clientes");
        return new ArchivoExportado(nombre,contenido);

    }

}