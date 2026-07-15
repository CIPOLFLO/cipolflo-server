package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.service.IConsultaClienteDetalle;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.PlazoConfirmacion;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final IConsultaClienteDetalle consultaClienteDetalle;

    public ReservaCreacionValidator(
            ReservaRepository reservaRepository,
            ServicioRepository servicioRepository,
            IConsultaClienteDetalle consultaClienteDetalle
    ) {
        this.reservaRepository = reservaRepository;
        this.servicioRepository = servicioRepository;
        this.consultaClienteDetalle = consultaClienteDetalle;
    }

    public void validar(ReservaCreacionRequestDto dto) {
        validarFechas(dto);
        validarServicio(dto.getServicioId());
        validarHoras(dto);
        validarSolapamiento(dto);
        validarCliente(dto);
        validarPlazoConfirmacion(dto);
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

    private void validarHoras(ReservaCreacionRequestDto dto) {
        ModalidadPrecio modalidad = servicioRepository.findById(dto.getServicioId())
                .orElseThrow()
                .getModalidadPrecio();

        boolean esPorHora = ModalidadPrecio.POR_HORA.equals(modalidad);

        if (esPorHora) {
            if (dto.getHoraInicio() == null || dto.getHoraFin() == null) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.HORA_REQUERIDA_PARA_SERVICIO_POR_HORA,
                        "El servicio requiere hora de inicio y hora de fin"
                );
            }
            boolean mismoDia = dto.getFechaInicio().isEqual(dto.getFechaFin());
            if (mismoDia && !dto.getHoraFin().isAfter(dto.getHoraInicio())) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.HORA_FIN_ANTERIOR_O_IGUAL_A_INICIO,
                        "La hora de fin debe ser posterior a la hora de inicio"
                );
            }
        } else {
            if (dto.getHoraInicio() != null || dto.getHoraFin() != null) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.HORA_NO_PERMITIDA_PARA_MODALIDAD,
                        "Las horas solo aplican a servicios con modalidad por hora"
                );
            }
        }
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
            return;
        }

        if (dto.getClienteId() == null) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.CLIENTE_REQUERIDO,
                    "Se requiere un clienteId para la reserva"
            );
        }

        // Si el cliente no existe, se deja propagar ClienteNotFoundException tal cual.
        ClienteDetalleReservaDto cliente = consultaClienteDetalle.getDetallClienteSimple(dto.getClienteId());

        boolean esColaboracion = dto.getTipoReserva() == TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO;
        if (esColaboracion && cliente.tipoCliente() != TipoCliente.EMPRESA) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.CLIENTE_EMPRESA_REQUERIDO_PARA_COLABORACION,
                    "Las reservas de colaboración sin fines de lucro requieren un cliente de tipo Empresa"
            );
        }
    }

    /**
     * El plazo de confirmación es obligatorio cuando la reserva requiere seña y/o
     * documentación, y no aplica en caso contrario. Cuando viene informado, además se valida
     * que la fecha límite de confirmación resultante no esté ya vencida (ej. reserva para
     * mañana con plazo de 3 meses), para que el job de cancelación automática no la cancele
     * en su siguiente corrida.
     */
    private void validarPlazoConfirmacion(ReservaCreacionRequestDto dto) {
        boolean requiereAlguno = Boolean.TRUE.equals(dto.getRequiereSena())
                || Boolean.TRUE.equals(dto.getRequiereDocumentacion());
        PlazoConfirmacion plazoConfirmacion = dto.getPlazoConfirmacion();

        if (requiereAlguno && plazoConfirmacion == null) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.PLAZO_CONFIRMACION_REQUERIDO,
                    "El plazo para confirmar la reserva es obligatorio cuando se requiere seña y/o documentación"
            );
        }
        if (!requiereAlguno && plazoConfirmacion != null) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.PLAZO_CONFIRMACION_NO_APLICA,
                    "El plazo para confirmar la reserva no aplica si no se requiere seña ni documentación"
            );
        }
        if (plazoConfirmacion != null) {
            LocalDateTime inicioReserva = dto.getFechaInicio().atTime(
                    dto.getHoraInicio() != null ? dto.getHoraInicio() : LocalTime.MIDNIGHT
            );
            LocalDateTime fechaLimiteConfirmacion = plazoConfirmacion.calcularFechaLimiteConfirmacion(inicioReserva);
            if (!fechaLimiteConfirmacion.isAfter(LocalDateTime.now(ZonaHoraria.URUGUAY))) {
                throw new ReservaValidacionException(
                        ReservaCodigoError.PLAZO_CONFIRMACION_VENCIDO,
                        "El plazo de confirmación seleccionado ya se encuentra vencido para la fecha de inicio indicada"
                );
            }
        }
    }
}