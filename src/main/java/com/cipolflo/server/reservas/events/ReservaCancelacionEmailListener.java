package com.cipolflo.server.reservas.events;

import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.reservas.scheduled.ReporteSemanalReservasProperties;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.shared.email.EmailException;
import com.cipolflo.server.shared.email.IEmailService;
import com.cipolflo.server.shared.email.SolicitudEmail;
import com.cipolflo.server.shared.email.TipoEventoEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Envía los mails de cancelación de reserva: al socio titular de cada reserva, y un único
 * mail consolidado a administración por lote cancelado.
 *
 * Se dispara DESPUÉS del commit de la transacción que canceló la/las reserva/s
 * ({@link TransactionPhase#AFTER_COMMIT}), corriendo en el pool {@code mailExecutor}: si la
 * transacción hace rollback, no se envía ningún mail.
 */
@Component
public class ReservaCancelacionEmailListener {

    private static final Logger log = LoggerFactory.getLogger(ReservaCancelacionEmailListener.class);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IReservaService reservaService;
    private final IEmailService emailService;
    private final ReporteSemanalReservasProperties reporteProperties;

    public ReservaCancelacionEmailListener(
            IReservaService reservaService,
            IEmailService emailService,
            ReporteSemanalReservasProperties reporteProperties
    ) {
        this.reservaService = reservaService;
        this.emailService = emailService;
        this.reporteProperties = reporteProperties;
    }

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservaCancelada(ReservaCanceladaEvent event) {
        List<Long> reservaIds = event.reservaIds();
        if (reservaIds == null || reservaIds.isEmpty()) {
            return;
        }

        List<ReservaDetalleResponseDto> detalles = reservaIds.stream()
                .map(reservaService::getDetalle)
                .toList();

        for (int i = 0; i < reservaIds.size(); i++) {
            enviarMailSocio(reservaIds.get(i), detalles.get(i), event.motivo());
        }

        enviarMailAdministracion(reservaIds, detalles, event.motivo());
    }

    private void enviarMailSocio(Long reservaId, ReservaDetalleResponseDto detalle, MotivoCancelacionReserva motivo) {
        ClienteDetalleReservaDto cliente = detalle.getCliente();
        String email = cliente != null ? cliente.email() : null;
        if (email == null || email.isBlank()) {
            log.debug("Reserva {} sin email de cliente; se omite el mail de cancelación al socio", reservaId);
            return;
        }

        String nombreSocio = (cliente.nombre() != null && !cliente.nombre().isBlank())
                ? cliente.nombre() : "socio/a";
        String nombreServicio = nombreServicio(detalle);
        String periodo = formatearPeriodo(detalle.getFechaEntrada(), detalle.getFechaSalida());

        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Estimado/a ").append(nombreSocio).append(":\n\n")
                .append("Le informamos que su reserva de ").append(nombreServicio)
                .append(" ").append(periodo).append(", ha sido cancelada.\n\n");

        if (motivo == MotivoCancelacionReserva.INHABILITACION_SERVICIO) {
            cuerpo.append("El motivo de la cancelación es que el servicio ").append(nombreServicio)
                    .append(" ha sido deshabilitado temporalmente.\n\n");
        }

        cuerpo.append("Por cualquier consulta, comuníquese con administración.\n\n")
                .append("Saludos cordiales,\n")
                .append("Comisión Directiva de Asociación Civil Círculo Policial de Flores");

        SolicitudEmail solicitud = SolicitudEmail.texto(
                email, "Cancelación de tu reserva", cuerpo.toString(),
                TipoEventoEmail.RESERVA_CANCELADA, reservaId);

        try {
            emailService.enviar(solicitud);
            log.info("Mail de cancelación enviado al socio para la reserva {}", reservaId);
        } catch (EmailException e) {
            log.error("Error enviando el mail de cancelación (socio) de la reserva {}: {}",
                    reservaId, e.getMessage(), e);
        }
    }

    private void enviarMailAdministracion(
            List<Long> reservaIds, List<ReservaDetalleResponseDto> detalles, MotivoCancelacionReserva motivo
    ) {
        String destinatarios = reporteProperties.destinatario();
        if (destinatarios == null || destinatarios.isBlank()) {
            log.debug("No hay destinatarios de administración configurados; se omite el aviso de cancelación");
            return;
        }

        String asunto = switch (motivo) {
            case MANUAL -> "Reserva cancelada";
            case BAJA_SOCIO -> "Reservas canceladas por baja de socio";
            case INHABILITACION_SERVICIO -> "Reservas canceladas por deshabilitación de servicio";
        };

        // El id de reserva no le sirve a administración: se identifica por cliente/servicio/fecha.
        String cuerpo = switch (motivo) {
            case MANUAL -> cuerpoCancelacionManual(detalles.get(0));
            case BAJA_SOCIO -> cuerpoCancelacionPorBajaSocio(detalles);
            case INHABILITACION_SERVICIO -> cuerpoCancelacionPorInhabilitacionServicio(detalles);
        };

        SolicitudEmail solicitud = SolicitudEmail.texto(
                destinatarios, asunto, cuerpo,
                TipoEventoEmail.RESERVA_CANCELADA, reservaIds.size() == 1 ? reservaIds.get(0) : null);

        try {
            emailService.enviar(solicitud);
            log.info("Mail de cancelación enviado a administración ({} reservas)", reservaIds.size());
        } catch (EmailException e) {
            log.error("Error enviando el mail de cancelación a administración: {}", e.getMessage(), e);
        }
    }

    private String cuerpoCancelacionManual(ReservaDetalleResponseDto detalle) {
        String periodo = formatearPeriodo(detalle.getFechaEntrada(), detalle.getFechaSalida());
        String base = "Se canceló la reserva de " + nombreCliente(detalle) + " para " + nombreServicio(detalle)
                + ", " + periodo + ".";

        String responsable = detalle.getUpdatedBy();
        return (responsable != null && !responsable.isBlank())
                ? base + "\n\nCancelación manual realizada por " + responsable + "."
                : base + "\n\nCancelación manual.";
    }

    // Socio constante en todo el lote: se aclara una sola vez y cada línea solo agrega servicio + fecha.
    private String cuerpoCancelacionPorBajaSocio(List<ReservaDetalleResponseDto> detalles) {
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Se dio de baja al socio ").append(nombreCliente(detalles.get(0)))
                .append(", por lo que se cancelaron las siguientes reservas:\n\n");
        for (ReservaDetalleResponseDto detalle : detalles) {
            cuerpo.append("  • ").append(nombreServicio(detalle)).append(" - ")
                    .append(formatearPeriodo(detalle.getFechaEntrada(), detalle.getFechaSalida())).append("\n");
        }
        return cuerpo.toString();
    }

    // Servicio constante en todo el lote: se aclara una sola vez y cada línea solo agrega cliente + fecha.
    private String cuerpoCancelacionPorInhabilitacionServicio(List<ReservaDetalleResponseDto> detalles) {
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Se deshabilitó el servicio ").append(nombreServicio(detalles.get(0)))
                .append(", por lo que se cancelaron las siguientes reservas:\n\n");
        for (ReservaDetalleResponseDto detalle : detalles) {
            cuerpo.append("  • ").append(nombreCliente(detalle)).append(" - ")
                    .append(formatearPeriodo(detalle.getFechaEntrada(), detalle.getFechaSalida())).append("\n");
        }
        return cuerpo.toString();
    }

    private String nombreServicio(ReservaDetalleResponseDto detalle) {
        ServicioDetalleReservaDto servicio = detalle.getServicio();
        return servicio != null && servicio.nombre() != null && !servicio.nombre().isBlank()
                ? servicio.nombre() : "el servicio reservado";
    }

    private String nombreCliente(ReservaDetalleResponseDto detalle) {
        ClienteDetalleReservaDto cliente = detalle.getCliente();
        return cliente != null && cliente.nombre() != null && !cliente.nombre().isBlank()
                ? cliente.nombre() : "(sin nombre)";
    }

    private String formatearPeriodo(LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (fechaEntrada.equals(fechaSalida)) {
            return "para el día " + FECHA.format(fechaEntrada);
        }
        return "para el período " + FECHA.format(fechaEntrada) + " - " + FECHA.format(fechaSalida);
    }
}
