package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.PlazoConfirmacion;
import com.cipolflo.server.reservas.dto.ReservaModificacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ReservaModificacionValidator {

    private final ReservaRepository reservaRepository;
    private final ServicioRepository servicioRepository;

    public ReservaModificacionValidator(ReservaRepository reservaRepository, ServicioRepository servicioRepository) {
        this.reservaRepository = reservaRepository;
        this.servicioRepository = servicioRepository;
    }

    public void validar(Reserva reserva, ReservaModificacionRequestDto dto) {
        validarFechas(reserva.getFechaEntrada(), reserva.getFechaSalida(), dto);
        validarServicio(dto.getServicioId());
        validarSolapamiento(reserva.getId(), dto);
        validarPlazoConfirmacion(reserva, dto);
    }

    private void validarFechas(LocalDate fechaEntrada, LocalDate fechaSalida, ReservaModificacionRequestDto dto) {
        if(!fechaEntrada.isEqual(dto.getFechaInicio()) ){
            LocalDate hoy = LocalDate.now(ZonaHoraria.URUGUAY);
            if (dto.getFechaInicio().isBefore(hoy)) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.FECHA_PASADA,
                        "La fecha de inicio no puede ser anterior a hoy"
                );
            }
        }
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.FECHA_FIN_ANTERIOR_A_INICIO,
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
    }

    private void validarServicio(Long servicioId) {
        servicioRepository.findById(servicioId)
                .filter(s -> Boolean.TRUE.equals(s.getHabilitado()))
                .orElseThrow(() -> new ReservaValidacionException(
                        ReservaCodigoError.SERVICIO_NO_DISPONIBLE,
                        "El servicio no está disponible"
                ));
    }

    /**
     * Si la reserva tiene plazoConfirmacion, la nueva fechaInicio no puede dejar la
     * fechaLimiteConfirmacion (recalculada por Reserva.modificar) ya vencida, para que el job
     * de cancelación automática no la cancele en su siguiente corrida.
     */
    private void validarPlazoConfirmacion(Reserva reserva, ReservaModificacionRequestDto dto) {
        PlazoConfirmacion plazoConfirmacion = reserva.getPlazoConfirmacion();
        if (plazoConfirmacion == null) {
            return;
        }
        LocalDateTime fechaLimiteConfirmacion =
                plazoConfirmacion.calcularFechaLimiteConfirmacion(dto.getFechaInicio(), reserva.getHoraInicio());
        if (!fechaLimiteConfirmacion.isAfter(LocalDateTime.now(ZonaHoraria.URUGUAY))) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.PLAZO_CONFIRMACION_VENCIDO,
                    "El plazo de confirmación ya se encuentra vencido para la nueva fecha de inicio indicada"
            );
        }
    }

    private void validarSolapamiento(Long reservaId, ReservaModificacionRequestDto dto) {
        boolean solapado = reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                        dto.getServicioId(),
                        EstadoReserva.ESTADOS_OCUPANTES,
                        dto.getFechaFin(),
                        dto.getFechaInicio(),
                        reservaId
                );
        if (solapado) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.FECHAS_SOLAPADAS,
                    "El servicio ya tiene una reserva activa en ese período"
            );
        }
    }
}
