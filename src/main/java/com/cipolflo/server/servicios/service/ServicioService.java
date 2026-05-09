package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.dto.ReservaProximaResponseDto;
import com.cipolflo.server.servicios.dto.ServicioHabilitacionResponseDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ServicioService implements IServicioService {
    private final ServicioRepository servicioRepository;
    private final ReservaRepository reservaRepository;

    public ServicioService(ServicioRepository servicioRepository,ReservaRepository reservaRepository ) {
        this.servicioRepository = servicioRepository;
        this.reservaRepository = reservaRepository;
    }

    public ServicioResponseDto getDetalleServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));
        return mapToResponse(servicio);
    }

    @Override
    public ServicioHabilitacionResponseDto cambiarHabilitacionServicio(Long id, ServicioRequestDto request) {
       Servicio servicio = servicioRepository.findById(id)
               .orElseThrow(() -> new ServicioNotFoundException(id));
        if (Boolean.TRUE.equals(request.getHabilitado())) {
            servicio.setHabilitado(true);

            Servicio servicioGuardado = servicioRepository.save(servicio);

            return new ServicioHabilitacionResponseDto(
                    mapToResponse(servicioGuardado),
                    List.of(),
                    "Servicio habilitado correctamente"
            );
        }

        List<Reserva> reservasProximas = obtenerReservasProximas(servicio.getId());

        if (reservasProximas.isEmpty()) {
            servicio.setHabilitado(false);

            Servicio servicioGuardado = servicioRepository.save(servicio);

            return new ServicioHabilitacionResponseDto(
                    mapToResponse(servicioGuardado),
                    List.of(),
                    "Servicio deshabilitado correctamente"
            );
        }

        if (!Boolean.TRUE.equals(request.getCancelarReservas())) {
            servicio.setHabilitado(false);

            Servicio servicioGuardado = servicioRepository.save(servicio);

            return new ServicioHabilitacionResponseDto(
                    mapToResponse(servicioGuardado),
                    mapReservasProximas(reservasProximas),
                    "Servicio deshabilitado. Existen reservas próximas que no fueron canceladas"
            );
        }

        List<Reserva> reservasSeleccionadas = reservaRepository.findByIdInAndServicioId(
                request.getReservasACancelar(),
                servicio.getId()
        );

        boolean existeReservaPaga = reservasSeleccionadas.stream()
                .anyMatch(reserva -> Boolean.TRUE.equals(reserva.getPago()));

        if (existeReservaPaga && !Boolean.TRUE.equals(request.getConfirmarDevolucion())) {
            throw new IllegalStateException("Existen reservas pagas. Debe confirmar la devolución para cancelarlas");
        }

        reservasSeleccionadas.forEach(Reserva::cancelar);
        reservaRepository.saveAll(reservasSeleccionadas);

        servicio.setHabilitado(false);
        Servicio servicioGuardado = servicioRepository.save(servicio);

        return new ServicioHabilitacionResponseDto(
                mapToResponse(servicioGuardado),
                mapReservasProximas(reservasSeleccionadas),
                "Servicio deshabilitado y reservas seleccionadas canceladas. Se debe notificar a los clientes afectados"
        );
    }

    private List<Reserva> obtenerReservasProximas(Long servicioId) {
        Instant desde = Instant.now();
        Instant hasta = desde.plus(60, ChronoUnit.DAYS);

        return reservaRepository.findByServicioIdAndFechaEntradaBetweenAndEstadoIn(
                servicioId,
                desde,
                hasta,
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );
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
