package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.dto.ModificacionServicioDto;
import com.cipolflo.server.servicios.dto.ReservaProximaResponseDto;
import com.cipolflo.server.servicios.dto.ServicioRegistroRequestDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.exception.ConfirmacionDevolucionRequeridaException;
import com.cipolflo.server.servicios.exception.ReservaNoCancelableException;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.mapper.ServicioMapper;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.servicios.validator.ModificacionServicioValidator;
import com.cipolflo.server.servicios.validator.ModificacionValidationContext;
import com.cipolflo.server.servicios.validator.ServicioRegistroValidator;
import com.cipolflo.server.servicios.repository.ServicioSpecification;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ServicioService implements IServicioService {
    private final ServicioRepository servicioRepository;
    private final IReservaService reservaService;
    private final ModificacionServicioValidator modificacionServicioValidator;
    private final ServicioRegistroValidator servicioRegistroValidator;
    public ServicioService(ServicioRepository servicioRepository,
                           IReservaService reservaService,
                           ModificacionServicioValidator modificacionServicioValidator,
                           ServicioRegistroValidator servicioRegistroValidator) {
        this.servicioRepository = servicioRepository;
        this.reservaService = reservaService;
        this.modificacionServicioValidator = modificacionServicioValidator;
        this.servicioRegistroValidator = servicioRegistroValidator;
    }

    @Override
    public ServicioResponseDto getDetalleServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));
        return mapToResponse(servicio);
    }

    @Override
    @Transactional
    public ServicioResponseDto cambiarHabilitacionServicio(Long id, ServicioRequestDto request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));

        if (Objects.equals(servicio.getHabilitado(), request.getHabilitado())) {
            return mapToResponse(servicio);
        }

        if (Boolean.TRUE.equals(request.getHabilitado())) {
            servicio.setHabilitado(true);
            return mapToResponse(servicioRepository.save(servicio));
        }

        cancelarReservasSiCorresponde(servicio, request);

        servicio.setHabilitado(false);
        return mapToResponse(servicioRepository.save(servicio));
    }

    @Override
    @Transactional
    public ServicioResponseDto modificarServicio(Long id, ModificacionServicioDto dto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));

        modificacionServicioValidator.validar(ModificacionValidationContext.from(id, dto));

        servicio.modificar(
                dto.getNombre(),
                dto.getPrecioParticular(),
                dto.getPrecioSocio(),
                dto.getModalidadPrecio(),
                dto.getCapacidad(),
                dto.getCantidad()
        );

        return mapToResponse(servicioRepository.save(servicio));
    }

    @Override
    public List<ReservaProximaResponseDto> getReservasProximas(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));

        return mapReservasProximas(
                reservaService.obtenerProximasPorServicioEnRango(servicio.getId())
        );
    }

    private void cancelarReservasSiCorresponde(Servicio servicio, ServicioRequestDto request) {
        if (request.getReservasACancelar() == null || request.getReservasACancelar().isEmpty()) {
            return;
        }

        List<Reserva> reservasProximasEnRango =
                reservaService.obtenerProximasPorServicioEnRango(servicio.getId());

        Set<Long> idsAcancelar = new HashSet<>(request.getReservasACancelar());
        List<Reserva> reservasSeleccionadasValidas = reservasProximasEnRango.stream()
                .filter(r -> idsAcancelar.contains(r.getId()))
                .toList();

        validarQueSeanReservasProximas(reservasSeleccionadasValidas, idsAcancelar);
        validarConfirmacionDevolucion(reservasSeleccionadasValidas, request);

        reservaService.cancelarTodas(reservasSeleccionadasValidas);
    }

    private void validarQueSeanReservasProximas(List<Reserva> reservasSeleccionadasValidas, Set<Long> idsAcancelar) {
        if (reservasSeleccionadasValidas.size() != idsAcancelar.size()) {
            throw new ReservaNoCancelableException();
        }
    }

    private void validarConfirmacionDevolucion(List<Reserva> reservasSeleccionadasValidas, ServicioRequestDto request) {
        boolean existeReservaPaga = reservasSeleccionadasValidas.stream()
                .anyMatch(reserva -> Boolean.TRUE.equals(reserva.getPago()));

        if (existeReservaPaga && !Boolean.TRUE.equals(request.getConfirmarDevolucion())) {
            throw new ConfirmacionDevolucionRequeridaException();
        }
    }

    private List<ReservaProximaResponseDto> mapReservasProximas(List<Reserva> reservas) {
        return reservas.stream()
                .map(this::mapReservaProxima)
                .toList();
    }

    @Override
    @Transactional
    public ServicioResponseDto registrarServicio(ServicioRegistroRequestDto request) {
        servicioRegistroValidator.validar(request);

        Servicio servicio = Servicio.registrar(
                request.getNombre(),
                request.getProcedencia(),
                request.getPrecioParticular(),
                request.getPrecioSocio(),
                request.getModalidadPrecio(),
                request.getCapacidad(),
                request.getCantidad()
        );

        return mapToResponse(servicioRepository.save(servicio));
    }

    private ReservaProximaResponseDto mapReservaProxima(Reserva reserva) {
        return new ReservaProximaResponseDto(
                reserva.getId(),
                reserva.getClienteId(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getPago(),
                reserva.getEstado()
        );
    }

    private ServicioResponseDto mapToResponse(Servicio servicio) {
        return new ServicioResponseDto(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getProcedencia(),
                servicio.getCantidad(),
                servicio.getPrecioSocio(),
                servicio.getPrecioParticular(),
                servicio.getCapacidad(),
                servicio.getHabilitado(),
                servicio.getModalidadPrecio()
        );
    }

    @Override
    public PageResponse<ListadoServiciosResponseDto> getListadoServicios(
            ListadoServiciosRequestDto filtros, PageRequestDto pageRequest) {

        Boolean habilitado = filtros.estado() != null ? filtros.estado().toBoolean() : null;

        Specification<Servicio> spec = ServicioSpecification.conNombre(filtros.nombre())
                .and(ServicioSpecification.conProcedencia(filtros.procedencia()))
                .and(ServicioSpecification.conHabilitado(habilitado));

        Page<ListadoServiciosResponseDto> page = servicioRepository
                .findAll(spec, pageRequest.toPageable())
                .map(ServicioMapper::toListadoResponseDto);

        return PaginationMapper.toPageResponse(page);
    }
}
