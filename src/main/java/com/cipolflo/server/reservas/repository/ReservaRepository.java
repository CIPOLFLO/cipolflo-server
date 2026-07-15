package com.cipolflo.server.reservas.repository;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {
    List<Reserva> findByServicioIdAndFechaEntradaBetweenAndEstadoIn(
            Long servicioId,
            LocalDate desde,
            LocalDate hasta,
            Collection<EstadoReserva> estados
    );

    List<Reserva> findByClienteIdAndFechaEntradaAfterAndEstadoIn(
            Long clienteId,
            LocalDate desde,
            Collection<EstadoReserva> estados
    );

    List<Reserva> findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
            Long servicioId,
            Collection<EstadoReserva> estados,
            LocalDate hasta,
            LocalDate desde
    );

    /**
     * Reservas en los estados dados que solapan el rango {@code [desde, hasta]}:
     * empezaron antes o durante el rango ({@code fechaEntrada <= hasta}) y siguen activas
     * dentro de él ({@code fechaSalida >= desde}). Usado por el reporte semanal.
     */
    List<Reserva> findByEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
            Collection<EstadoReserva> estados,
            LocalDate hasta,
            LocalDate desde
    );

    boolean existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
            Long servicioId,
            Collection<EstadoReserva> estados,
            LocalDate fechaFin,
            LocalDate fechaInicio
    );

    boolean existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
            Long servicioId,
            Collection<EstadoReserva> estados,
            LocalDate fechaFin,
            LocalDate fechaInicio,
            Long idExcluir
    );

    /**
     * Reservas en el estado dado cuya fecha límite de confirmación ya se cumplió o pasó.
     * Usada por el job de cancelación automática, invocada con PENDIENTE y la fecha/hora
     * actual: el guard de estado va acá (en la query), no en memoria, para que el job nunca
     * pueda tocar una reserva CONFIRMADA, EN_CURSO, FINALIZADA ni CANCELADA.
     */
    List<Reserva> findByEstadoAndFechaLimiteConfirmacionLessThanEqual(
            EstadoReserva estado,
            LocalDateTime limite
    );

    /**
     * Reservas en el estado dado cuya fecha de entrada es exactamente {@code fechaEntrada}.
     * Usado por el scheduler de transición de estados por fecha (paso a EN_CURSO).
     */
    List<Reserva> findByEstadoAndFechaEntrada(EstadoReserva estado, LocalDate fechaEntrada);

    /**
     * Reservas en el estado dado cuya fecha de salida es exactamente {@code fechaSalida}.
     * Usado por el scheduler de transición de estados por fecha (paso a FINALIZADA/VENCIDA_SIN_PAGO).
     */
    List<Reserva> findByEstadoAndFechaSalida(EstadoReserva estado, LocalDate fechaSalida);
}