package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.service.IConsultaClienteDetalle;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.servicios.service.IConsultaServicioSimple;
import com.cipolflo.server.shared.ZonaHoraria;
import com.cipolflo.server.shared.email.IConsultaDestinatariosNotificacionEmail;
import com.cipolflo.server.shared.email.IEmailService;
import com.cipolflo.server.shared.email.SolicitudEmail;
import com.cipolflo.server.shared.email.TipoEventoEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Arma el reporte semanal de reservas y lo envía al destinatario configurado.
 *
 * La semana reportada es la que arranca el lunes actual (hora de Uruguay), de lunes
 * a domingo. Se incluyen tres secciones disjuntas, en este orden:
 * <ol>
 *   <li><b>En curso</b>: reservas CONFIRMADA o EN_CURSO que empezaron antes del lunes
 *       y siguen activas dentro de la semana (arrastre).</li>
 *   <li><b>Confirmadas</b>: reservas CONFIRMADA que inician dentro de la semana.</li>
 *   <li><b>Pendientes</b>: reservas PENDIENTE que inician dentro de la semana (arrancan
 *       esta semana pero aún no se confirmaron: falta documentación o falta pagar la seña).</li>
 * </ol>
 * Las reservas sin cliente asociado (temporales) se omiten por ahora.
 */
@Service
public class ReporteSemanalReservasService implements IReporteSemanalReservasService {

    private static final Logger log = LoggerFactory.getLogger(ReporteSemanalReservasService.class);

    private static final List<EstadoReserva> ESTADOS_REPORTE = List.of(
            EstadoReserva.PENDIENTE,
            EstadoReserva.CONFIRMADA,
            EstadoReserva.EN_CURSO,
            EstadoReserva.VENCIDA_SIN_PAGO
    );

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Comparator<Reserva> POR_FECHA =
            Comparator.comparing(Reserva::getFechaEntrada).thenComparing(Reserva::getFechaSalida);

    private final ReservaRepository reservaRepository;
    private final IConsultaServicioSimple consultaServicioSimple;
    private final IConsultaClienteDetalle consultaClienteDetalle;
    private final IEmailService emailService;
    private final IConsultaDestinatariosNotificacionEmail consultaDestinatarios;

    public ReporteSemanalReservasService(
            ReservaRepository reservaRepository,
            IConsultaServicioSimple consultaServicioSimple,
            IConsultaClienteDetalle consultaClienteDetalle,
            IEmailService emailService,
            IConsultaDestinatariosNotificacionEmail consultaDestinatarios
    ) {
        this.reservaRepository = reservaRepository;
        this.consultaServicioSimple = consultaServicioSimple;
        this.consultaClienteDetalle = consultaClienteDetalle;
        this.emailService = emailService;
        this.consultaDestinatarios = consultaDestinatarios;
    }

    @Override
    public String enviarReporteSemanal() {
        String destinatario = consultaDestinatarios.destinatariosActivos();
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("Reporte semanal de reservas: no hay destinatarios activos configurados "
                    + "en Ajustes > Destinatarios de notificación por email; se omite el envío.");
            return "Omitido: no hay destinatario configurado.";
        }

        LocalDate lunes = LocalDate.now(ZonaHoraria.URUGUAY)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);

        // fechaEntrada <= domingo AND fechaSalida >= lunes  →  reservas que solapan la semana.
        List<Reserva> reservas = reservaRepository
                .findByEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        ESTADOS_REPORTE, domingo, lunes);

        ReservasSemana categorizadas = categorizar(reservas, lunes, domingo);
        String cuerpo = construirCuerpo(categorizadas, lunes, domingo);

        String asunto = "Reservas de la semana del %s al %s".formatted(FECHA.format(lunes), FECHA.format(domingo));

        SolicitudEmail solicitud = SolicitudEmail.texto(
                destinatario, asunto, cuerpo, TipoEventoEmail.REPORTE_SEMANAL_RESERVAS, null);

        // Si el envío falla se propaga la EmailException: el ejecutor la registra como
        // ejecución FALLIDA de la tarea (además del registro en envio_emails_logs).
        emailService.enviar(solicitud);

        return "Enviado a %s: %d en curso, %d confirmadas, %d pendientes.".formatted(
                destinatario, categorizadas.enCurso().size(),
                categorizadas.confirmadas().size(), categorizadas.pendientes().size());
    }

    /**
     * Reparte las reservas en las tres secciones (disjuntas) del reporte. Descarta las
     * que no encajan en ninguna (ej. reservas sin cliente, o que solapan la semana pero
     * no cumplen la regla de fecha de su estado).
     */
    static ReservasSemana categorizar(List<Reserva> reservas, LocalDate lunes, LocalDate domingo) {
        List<Reserva> enCurso = new ArrayList<>();
        List<Reserva> confirmadas = new ArrayList<>();
        List<Reserva> pendientes = new ArrayList<>();

        for (Reserva r : reservas) {
            // TODO: temporal - se ignoran reservas sin cliente asociado hasta que todas lo tengan.
            if (r.getClienteId() == null) {
                continue;
            }
            EstadoReserva estado = r.getEstado();
            // La query ya garantiza fechaSalida >= lunes (solape con la semana), así que basta
            // con que la reserva haya empezado antes del lunes para ser "arrastre".
            boolean empiezaAntes = r.getFechaEntrada().isBefore(lunes);
            boolean iniciaEnSemana = !r.getFechaEntrada().isBefore(lunes) && !r.getFechaEntrada().isAfter(domingo);

            if ((estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EN_CURSO
                    || estado == EstadoReserva.VENCIDA_SIN_PAGO)
                    && empiezaAntes) {
                enCurso.add(r);
            } else if (estado == EstadoReserva.CONFIRMADA && iniciaEnSemana) {
                confirmadas.add(r);
            } else if (estado == EstadoReserva.PENDIENTE && iniciaEnSemana) {
                pendientes.add(r);
            }
        }

        enCurso.sort(POR_FECHA);
        confirmadas.sort(POR_FECHA);
        pendientes.sort(POR_FECHA);
        return new ReservasSemana(enCurso, confirmadas, pendientes);
    }

    private String construirCuerpo(ReservasSemana semana, LocalDate lunes, LocalDate domingo) {
        Map<Long, String> nombresServicios = resolverNombresServicios(semana);
        Map<Long, ClienteDetalleReservaDto> clientes = resolverClientes(semana);

        StringBuilder sb = new StringBuilder();
        sb.append("Reporte de reservas de la semana del ")
                .append(FECHA.format(lunes)).append(" al ").append(FECHA.format(domingo))
                .append(".\n\n");

        agregarSeccion(sb, "EN CURSO (arrastre de semanas anteriores)", semana.enCurso(), nombresServicios, clientes);
        agregarSeccion(sb, "CONFIRMADAS (inician esta semana)", semana.confirmadas(), nombresServicios, clientes);
        agregarSeccion(sb, "PENDIENTES (inician esta semana, sin confirmar)", semana.pendientes(), nombresServicios, clientes);

        return sb.toString();
    }

    private void agregarSeccion(StringBuilder sb, String titulo, List<Reserva> reservas,
                                Map<Long, String> nombresServicios,
                                Map<Long, ClienteDetalleReservaDto> clientes) {
        sb.append(titulo).append(" (").append(reservas.size()).append(")");
        if (reservas.isEmpty()) {
            sb.append("  Sin reservas.\n\n");
            return;
        }
        for (Reserva r : reservas) {
            String servicio = nombresServicios.getOrDefault(r.getServicioId(), "(servicio #" + r.getServicioId() + ")");
            ClienteDetalleReservaDto cliente = clientes.get(r.getClienteId());
            String nombreCliente = cliente != null && cliente.nombre() != null ? cliente.nombre() : "(sin nombre)";
            String telefono = cliente != null && cliente.telefono() != null && !cliente.telefono().isBlank()
                    ? cliente.telefono() : "sin teléfono";
            sb.append("  • ").append(servicio)
                    .append(" | ").append(nombreCliente).append(" (").append(telefono).append(")")
                    .append(" | ").append(FECHA.format(r.getFechaEntrada()))
                    .append(" a ").append(FECHA.format(r.getFechaSalida()))
                    .append("\n");
        }
        sb.append("\n");
    }

    private Map<Long, String> resolverNombresServicios(ReservasSemana semana) {
        Set<Long> ids = semana.todas()
                .map(Reserva::getServicioId).filter(Objects::nonNull).collect(Collectors.toSet());
        return ids.isEmpty() ? Map.of() : consultaServicioSimple.getNombresByIds(ids);
    }

    private Map<Long, ClienteDetalleReservaDto> resolverClientes(ReservasSemana semana) {
        // Volumen semanal bajo: se resuelve cliente por cliente (necesitamos nombre + teléfono,
        // que getNombresByIds no trae). Si el volumen crece, agregar un getDetallesByIds en bloque.
        Set<Long> ids = semana.todas()
                .map(Reserva::getClienteId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ClienteDetalleReservaDto> map = new HashMap<>();
        for (Long id : ids) {
            map.put(id, consultaClienteDetalle.getDetallClienteSimple(id));
        }
        return map;
    }

    /**
     * Reservas de la semana ya repartidas en sus tres secciones.
     */
    record ReservasSemana(List<Reserva> enCurso, List<Reserva> confirmadas, List<Reserva> pendientes) {
        Stream<Reserva> todas() {
            return Stream.of(enCurso, confirmadas, pendientes).flatMap(List::stream);
        }
    }
}
