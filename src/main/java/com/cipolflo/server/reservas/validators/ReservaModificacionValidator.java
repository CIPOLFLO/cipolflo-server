package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ReservaModificacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReservaModificacionValidator {

    private static final List<EstadoReserva> ESTADOS_OCUPANTES = List.of(
            EstadoReserva.PENDIENTE,
            EstadoReserva.CONFIRMADA,
            EstadoReserva.EN_CURSO
    );

    private final ReservaRepository reservaRepository;
    private final ServicioRepository servicioRepository;

    public ReservaModificacionValidator(ReservaRepository reservaRepository, ServicioRepository servicioRepository) {
        this.reservaRepository = reservaRepository;
        this.servicioRepository = servicioRepository;
    }

    public void validar(Reserva reserva, ReservaModificacionRequestDto dto) {
        validarFechas(reserva.getFechaEntrada(), reserva.getFechaSalida() ,dto);
        validarServicio(dto.getServicioId());
        validarSolapamiento(reserva.getId(), dto);
        validarRut(reserva.getTipoReserva(), dto.getRut());
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

    private void validarSolapamiento(Long reservaId, ReservaModificacionRequestDto dto) {
        boolean solapado = reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                        dto.getServicioId(),
                        ESTADOS_OCUPANTES,
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

    private void validarRut(TipoReserva tipoReserva, String rut) {
        boolean esColaboracion = TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO.equals(tipoReserva);
        if (StringUtils.hasText(rut) && !esColaboracion) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.RUT_SOLO_VALIDO_EN_COLABORACION,
                    "El RUT solo es válido para reservas de colaboración sin fines de lucro"
            );
        }
    }
}
