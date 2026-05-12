package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.dto.ReservaProximaResponseDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.exception.CampoObligatorioException;
import com.cipolflo.server.servicios.exception.ConfirmacionDevolucionRequeridaException;
import com.cipolflo.server.servicios.exception.ReservaNoCancelableException;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioService implements IServicioService {
    private final ServicioRepository servicioRepository;
    private final IReservaService reservaService;

    public ServicioService(ServicioRepository servicioRepository,IReservaService reservaService ) {
        this.servicioRepository = servicioRepository;
        this.reservaService = reservaService;
    }

    public ServicioResponseDto getDetalleServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));
        return mapToResponse(servicio);
    }

    @Override
    @Transactional
    public ServicioResponseDto  cambiarHabilitacionServicio(Long id, ServicioRequestDto request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));

        if (Boolean.TRUE.equals(servicio.getHabilitado()) == Boolean.TRUE.equals(request.getHabilitado()))  {
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

        List<Reserva> reservasSeleccionadas =
                reservaService.obtenerPorIdsYServicio(
                        request.getReservasACancelar(),
                        servicio.getId()
                );

        validarQueSeanReservasProximas(reservasSeleccionadas, reservasProximasEnRango);
        validarConfirmacionDevolucion(reservasSeleccionadas, request);

        reservaService.cancelarTodas(reservasSeleccionadas);
    }

    private void validarQueSeanReservasProximas(List<Reserva> reservasSeleccionadas, List<Reserva> reservasProximas) {
        List<Long> idsReservasProximas = reservasProximas.stream()
                .map(Reserva::getId)
                .toList();

        boolean todasSonReservasProximas = reservasSeleccionadas.stream()
                .allMatch(reserva -> idsReservasProximas.contains(reserva.getId()));

        if (!todasSonReservasProximas) {
            throw new ReservaNoCancelableException();
        }
    }

    private void validarConfirmacionDevolucion(List<Reserva> reservasSeleccionadas, ServicioRequestDto request) {
        boolean existeReservaPaga = reservasSeleccionadas.stream()
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

}
