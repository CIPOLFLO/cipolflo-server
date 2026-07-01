package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.RegistroParticularRequestDto;
import com.cipolflo.server.clientes.service.IConsultaClienteDetalle;
import com.cipolflo.server.clientes.service.IRegistroParticularService;
import com.cipolflo.server.reservas.mapper.ReservaMapper;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.*;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.repository.ReservaSpecification;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.ReservaCreacionValidator;
import com.cipolflo.server.reservas.validators.ReservaModificacionValidator;
import com.cipolflo.server.servicios.service.IConsultaServicioSimple;
import com.cipolflo.server.servicios.service.IServicioRequiereDocumentacion;
import com.cipolflo.server.shared.ZonaHoraria;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.export.ExportProperties;
import com.cipolflo.server.shared.export.ExportacionException;
import com.cipolflo.server.shared.export.IExportService;
import com.cipolflo.server.shared.export.NombreArchivoExport;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservaService implements IReservaService {
    private static final int DIAS_VENTANA_RESERVAS_PROXIMAS = 60;

    private static final List<EstadoReserva> ESTADOS_OCUPANTES = List.of(
            EstadoReserva.PENDIENTE,
            EstadoReserva.CONFIRMADA,
            EstadoReserva.EN_CURSO
    );

    private final ReservaRepository reservaRepository;
    private final IRegistroParticularService registroParticularService;
    private final ReservaCreacionValidator reservaCreacionValidator;
    private final ReservaModificacionValidator reservaModificacionValidator;
    private final IServicioRequiereDocumentacion servicioRequiereDocumentacion;
    private final IConsultaClienteDetalle consultaClienteDetalle;
    private final IConsultaServicioSimple consultaServicioSimple;
    private final ICalculoCostoService calculoCostoService;
    private final ExportProperties exportProperties;
    private final IExportService exportService;
    public ReservaService(
            ReservaRepository reservaRepository,
            IRegistroParticularService registroParticularService,
            ReservaCreacionValidator reservaCreacionValidator,
            ReservaModificacionValidator reservaModificacionValidator,
            IServicioRequiereDocumentacion servicioRequiereDocumentacion,
            IConsultaServicioSimple consultaServicioSimple,
            IConsultaClienteDetalle consultaClienteDetalle,
            ICalculoCostoService calculoCostoService,
            ExportProperties exportProperties,
            IExportService exportService
    ) {
        this.reservaRepository = reservaRepository;
        this.registroParticularService = registroParticularService;
        this.reservaCreacionValidator = reservaCreacionValidator;
        this.reservaModificacionValidator = reservaModificacionValidator;
        this.servicioRequiereDocumentacion = servicioRequiereDocumentacion;
        this.consultaClienteDetalle = consultaClienteDetalle;
        this.consultaServicioSimple = consultaServicioSimple;
        this.calculoCostoService = calculoCostoService;
        this.exportProperties = exportProperties;
        this.exportService = exportService;
    }

    @Override
    public List<Reserva> obtenerProximasPorServicioEnRango(Long servicioId) {
        LocalDate desde = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate hasta = desde.plusDays(DIAS_VENTANA_RESERVAS_PROXIMAS);

        return reservaRepository.findByServicioIdAndFechaEntradaBetweenAndEstadoIn(
                servicioId,
                desde,
                hasta,
                List.of(
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA
                )
        );
    }

    @Override
    public List<Reserva> obtenerOcupacionPorServicioEnRango(Long servicioId, LocalDate desde, LocalDate hasta) {
        return reservaRepository
                .findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        servicioId,
                        ESTADOS_OCUPANTES,
                        hasta,
                        desde
                );
    }

    @Override
    public void cancelarTodas(List<Reserva> reservas) {
        reservas.forEach(Reserva::cancelar);
        reservaRepository.saveAll(reservas);
    }

    @Override
    public void cancelarReservasFuturasPorCliente(Long clienteId) {
        List<Reserva> reservas = reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                clienteId,
                LocalDate.now(ZonaHoraria.URUGUAY),
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );
        cancelarTodas(reservas);
    }

    @Override
    @Transactional
    public ReservaCreacionResponseDto registrar(ReservaCreacionRequestDto dto) {
        reservaCreacionValidator.validar(dto);

        Long clienteId;
        if (Boolean.TRUE.equals(dto.getCrearCliente())) {
            RegistroParticularRequestDto nuevoCliente = new RegistroParticularRequestDto();
            nuevoCliente.setNombre(dto.getNombre());
            nuevoCliente.setCedula(dto.getCedula());
            nuevoCliente.setCelular(dto.getCelular());
            nuevoCliente.setMail(dto.getEmail());

            ClienteResponseDto clienteCreado = registroParticularService.registrarParticular(nuevoCliente);
            clienteId = clienteCreado.getId();
        } else {
            clienteId = dto.getClienteId();
        }

        boolean requiereDocumentacion = servicioRequiereDocumentacion.requiereDocumentacion(dto.getServicioId());

        CalculoCostoRequestDto calculoCostoRequest = getCalculoCostoRequestDto(dto);
        CalculoCostoResponseDto calculoCosto = calculoCostoService.calcularCosto(calculoCostoRequest);

        Reserva reserva = Reserva.crear(
                dto.getTipoReserva(),
                clienteId,
                dto.getServicioId(),
                dto.getProcedencia(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                dto.getHoraInicio(),
                dto.getHoraFin(),
                dto.getCantidadTotal(),
                dto.getCantidadMenores(),
                dto.getCantidad(),
                dto.getRut(),
                dto.getNotas(),
                requiereDocumentacion,
                calculoCosto.costoTotal(),
                dto.getFechaLimite(),
                dto.getNombre()

        );

        Reserva guardada = reservaRepository.save(reserva);

        return new ReservaCreacionResponseDto(guardada.getId());
    }

    @Override
    public ReservaDetalleResponseDto getDetalle(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));
        ClienteDetalleReservaDto cliente = reserva.getClienteId() != null
                ? consultaClienteDetalle.getDetallClienteSimple(reserva.getClienteId())
                : null;
        ServicioDetalleReservaDto servicio =
                consultaServicioSimple.getDetalleServicioSimple(reserva.getServicioId());
        return ReservaMapper.toDetalleResponseDto(reserva, cliente, servicio);
    }

    @Override
    public PageResponse<ListadoReservasResponseDto> getListadoReservas(ListadoReservasRequestDto filtros, PageRequestDto pageRequest) {
        List<Long> clienteIds = null;
        if (filtros.nombreCliente() != null && !filtros.nombreCliente().isBlank()) {
            clienteIds = consultaClienteDetalle.getIdsByNombre(filtros.nombreCliente());
            if (clienteIds.isEmpty()) {
                return new PageResponse<>(List.of(), pageRequest.page(), pageRequest.size(), 0, 0, true, true);
            }
        }

        Specification<Reserva> spec = ReservaSpecification.conProcedencia(filtros.procedencia())
                .and(ReservaSpecification.conServicioId(filtros.servicioId()))
                .and(ReservaSpecification.conEstado(filtros.estadoReserva()))
                .and(ReservaSpecification.conFechaEntradaDesde(filtros.fechaDesde()))
                .and(ReservaSpecification.conFechaSalidaHasta(filtros.fechaHasta()))
                .and(ReservaSpecification.conClienteIds(clienteIds));

        Pageable pageable;
        if ("nombreCliente".equals(pageRequest.sortField())) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(pageRequest.sortOrder())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            spec = spec.and(ReservaSpecification.ordenadoPorNombreCliente(direction));
            pageable = PageRequest.of(pageRequest.page(), pageRequest.size());
        } else {
            pageable = pageRequest.toPageable();
        }

        Page<Reserva> page = reservaRepository.findAll(spec, pageable);

        Set<Long> clienteIdsPage = page.getContent().stream()
                .map(Reserva::getClienteId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nombresClientes = consultaClienteDetalle.getNombresByIds(clienteIdsPage);

        Set<Long> servicioIdsPage = page.getContent().stream()
                .map(Reserva::getServicioId).collect(Collectors.toSet());
        Map<Long, String> nombresServicios = consultaServicioSimple.getNombresByIds(servicioIdsPage);

        Page<ListadoReservasResponseDto> dtoPage = page.map(r -> new ListadoReservasResponseDto(
                r.getId(),
                r.getClienteId(),
                // TODO: temporal - usar nombreRut como nombre de cliente para reservas sin fines de lucro hasta definir manejo de clientes RUT
                r.getClienteId() != null ? nombresClientes.get(r.getClienteId()) : r.getNombreRut(),
                r.getServicioId(),
                nombresServicios.get(r.getServicioId()),
                r.getFechaEntrada(),
                r.getFechaSalida(),
                r.getEstado(),
                r.getMontoImpago(),
                r.getFechaLimitePago(),
                r.getRequiereDocumentacion() && !r.getTieneDocumentacion()
        ));

        return PaginationMapper.toPageResponse(dtoPage);
    }

    @Override
    @Transactional
    public ReservaModificacionResponseDto modificar(Long id, ReservaModificacionRequestDto dto) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));

        if (reserva.getEstado() != EstadoReserva.PENDIENTE && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.RESERVA_NO_MODIFICABLE,
                    "La reserva no puede ser modificada en su estado actual"
            );
        }

        reservaModificacionValidator.validar(reserva, dto);

        reserva.modificar(
                dto.getServicioId(),
                dto.getProcedencia(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                dto.getCantidadTotal(),
                dto.getCantidadMenores(),
                dto.getCantidad(),
                dto.getRut(),
                dto.getNotas()
        );

        reservaRepository.save(reserva);

        return new ReservaModificacionResponseDto(reserva.getId());
    }

    @Override
    public CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request) {
        return calculoCostoService.calcularCosto(request);
    }

    @Nonnull
    private static CalculoCostoRequestDto getCalculoCostoRequestDto(ReservaCreacionRequestDto dto) {
        CalculoCostoRequestDto calculoCostoRequest = new CalculoCostoRequestDto();
        calculoCostoRequest.setServicioId(dto.getServicioId());
        calculoCostoRequest.setFechaInicio(dto.getFechaInicio());
        calculoCostoRequest.setFechaFin(dto.getFechaFin());
        calculoCostoRequest.setHoraInicio(dto.getHoraInicio());
        calculoCostoRequest.setHoraFin(dto.getHoraFin());
        calculoCostoRequest.setCantidadTotal(dto.getCantidadTotal());
        calculoCostoRequest.setCantidad(dto.getCantidad());
        calculoCostoRequest.setCantidadMenores(dto.getCantidadMenores());
        calculoCostoRequest.setTipoCliente(dto.getTipoCliente());
        return calculoCostoRequest;
    }

    @Override
    public ArchivoExportado exportarReservas(ListadoReservasRequestDto filtros) {
        List<Long> clienteIds = null;
        if (filtros.nombreCliente() != null && !filtros.nombreCliente().isBlank()) {
            clienteIds = consultaClienteDetalle.getIdsByNombre(filtros.nombreCliente());
            if (clienteIds.isEmpty()) {
                throw new ExportacionException("No hay registros que coincidan con los filtros aplicados");
            }
        }

        Specification<Reserva> spec = ReservaSpecification.conProcedencia(filtros.procedencia())
                .and(ReservaSpecification.conServicioId(filtros.servicioId()))
                .and(ReservaSpecification.conEstado(filtros.estadoReserva()))
                .and(ReservaSpecification.conFechaEntradaDesde(filtros.fechaDesde()))
                .and(ReservaSpecification.conFechaSalidaHasta(filtros.fechaHasta()))
                .and(ReservaSpecification.conClienteIds(clienteIds));

        List<Reserva> reservas = reservaRepository.findAll(spec);

        if (reservas.isEmpty()) {
            throw new ExportacionException("No hay registros que coincidan con los filtros aplicados");
        }

        if (reservas.size() > exportProperties.maxFilas()) {
            throw new ExportacionException(
                    "La exportación supera el límite de " + exportProperties.maxFilas() + " filas"
            );
        }

        Set<Long> clienteIdsExport = reservas.stream()
                .map(Reserva::getClienteId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nombresClientes = consultaClienteDetalle.getNombresByIds(clienteIdsExport);

        Set<Long> servicioIdsExport = reservas.stream()
                .map(Reserva::getServicioId).collect(Collectors.toSet());
        Map<Long, String> nombresServicios = consultaServicioSimple.getNombresByIds(servicioIdsExport);

        List<String> encabezados = List.of(
                "ID",
                "Tipo de reserva",
                "Estado",
                "Procedencia",
                "Servicio",
                "Cliente",
                "Fecha entrada",
                "Fecha salida",
                "Hora inicio",
                "Hora fin",
                "Importe",
                "Pago confirmado",
                "Cantidad total",
                "Cantidad menores",
                "Cantidad articulos",
                "Requiere documentación",
                "Tiene documentación",
                "Notas"
        );

        List<List<String>> filas = reservas.stream()
                .map(r -> ReservaMapper.toExportFila(
                        r,
                        r.getClienteId() != null ? nombresClientes.get(r.getClienteId()) : null,
                        nombresServicios.get(r.getServicioId())
                ))
                .toList();

        int[] anchos = {3000, 8000, 6000, 6000, 6000, 6000, 5000, 5000, 8000, 8000, 5000, 6000, 6000, 5000, 5000, 5000, 7000, 7000, 8000};

        byte[] contenido = exportService.generarExcel("Reservas", encabezados, filas, anchos);

        String nombre = NombreArchivoExport.generar("reservas");
        return new ArchivoExportado(nombre, contenido);
}
}
