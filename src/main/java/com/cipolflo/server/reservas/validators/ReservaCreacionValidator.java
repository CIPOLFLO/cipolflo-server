package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
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
public class ReservaCreacionValidator {

    private static final List<EstadoReserva> ESTADOS_OCUPANTES = List.of(
            EstadoReserva.PENDIENTE,
            EstadoReserva.CONFIRMADA,
            EstadoReserva.EN_CURSO
    );

    private final ReservaRepository reservaRepository;
    private final ServicioRepository servicioRepository;

    public ReservaCreacionValidator(ReservaRepository reservaRepository, ServicioRepository servicioRepository) {
        this.reservaRepository = reservaRepository;
        this.servicioRepository = servicioRepository;
    }

    public void validar(ReservaCreacionRequestDto dto) {
        validarFechas(dto);
        validarServicio(dto.getServicioId());
        validarSolapamiento(dto);
        validarCliente(dto);
    }

    private void validarFechas(ReservaCreacionRequestDto dto) {
        LocalDate hoy = LocalDate.now(ZonaHoraria.URUGUAY);
        if (dto.getFechaInicio().isBefore(hoy)) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.FECHA_PASADA,
                    "La fecha de inicio no puede ser anterior a hoy"
            );
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

    private void validarSolapamiento(ReservaCreacionRequestDto dto) {
        boolean solapado = reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        dto.getServicioId(),
                        ESTADOS_OCUPANTES,
                        dto.getFechaFin(),
                        dto.getFechaInicio()
                );
        if (solapado) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.FECHAS_SOLAPADAS,
                    "El servicio ya tiene una reserva activa en ese período"
            );
        }
    }

    private void validarCliente(ReservaCreacionRequestDto dto) {
        if (Boolean.TRUE.equals(dto.getCrearCliente())) {
            if (!StringUtils.hasText(dto.getNombre())) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.NOMBRE_REQUERIDO_PARA_CREAR_CLIENTE,
                        "El nombre es requerido para crear el cliente"
                );
            }
            if (!StringUtils.hasText(dto.getCedula())) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.CEDULA_REQUERIDA_PARA_CREAR_CLIENTE,
                        "La cédula es requerida para crear el cliente"
                );
            }
            if (!StringUtils.hasText(dto.getCelular())) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.CELULAR_REQUERIDO_PARA_CREAR_CLIENTE,
                        "El celular es requerido para crear el cliente"
                );
            }
        } else if (dto.getClienteId() == null) {
            boolean esColaboracion = TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO.equals(dto.getTipoReserva());
            if (StringUtils.hasText(dto.getRut()) && !esColaboracion) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.RUT_SOLO_VALIDO_EN_COLABORACION,
                        "El RUT solo es válido para reservas de colaboración sin fines de lucro"
                );
            }
            if (!StringUtils.hasText(dto.getRut())) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.CLIENTE_REQUERIDO,
                        "Se requiere un clienteId o RUT para la reserva"
                );
            }
        }
    }
}
