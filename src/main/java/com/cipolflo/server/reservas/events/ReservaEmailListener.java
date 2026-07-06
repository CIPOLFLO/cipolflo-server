package com.cipolflo.server.reservas.events;

import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
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

/**
 * Envía el mail de confirmación de reserva.
 *
 * Se dispara DESPUÉS de que la transacción de {@code registrar} hizo commit
 * ({@link TransactionPhase#AFTER_COMMIT}): así nunca se manda un mail de una reserva
 * que terminó en rollback. Corre en el pool {@code mailExecutor} ({@code @Async}) para
 * no bloquear la respuesta HTTP mientras se conecta al SMTP.
 */
@Component
public class ReservaEmailListener {

    private static final Logger log = LoggerFactory.getLogger(ReservaEmailListener.class);

    private final IReservaService reservaService;
    private final IEmailService emailService;

    public ReservaEmailListener(IReservaService reservaService, IEmailService emailService) {
        this.reservaService = reservaService;
        this.emailService = emailService;
    }

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservaCreada(ReservaCreadaEvent event) {
        ReservaDetalleResponseDto detalle = reservaService.getDetalle(event.reservaId());
        ClienteDetalleReservaDto cliente = detalle.getCliente();

        String email = cliente != null ? cliente.email() : null;
        if (email == null || email.isBlank()) {
            log.debug("Reserva {} sin email de cliente; se omite la notificación", event.reservaId());
            return;
        }

        String nombre = (cliente.nombre() != null && !cliente.nombre().isBlank())
                ? cliente.nombre()
                : "cliente";

        // TODO: ajustar asunto/cuerpo al copy definitivo. Más adelante se puede adjuntar el PDF
        //       del comprobante usando SolicitudEmail.conAdjuntos(...).
        String asunto = "Confirmación de tu reserva #" + event.reservaId();
        String cuerpo = """
                Hola %s,

                Recibimos tu reserva (#%d). En breve te confirmaremos los detalles.

                Saludos,
                CIPOLFLO""".formatted(nombre, event.reservaId());

        SolicitudEmail solicitud = SolicitudEmail.texto(
                email, asunto, cuerpo, TipoEventoEmail.RESERVA_CREADA, event.reservaId());

        try {
            emailService.enviar(solicitud);
            log.info("Mail de confirmación enviado para la reserva {}", event.reservaId());
        } catch (EmailException e) {
            // No relanzamos: el mail es un efecto secundario, la reserva ya se guardó.
            log.error("Error enviando el mail de la reserva {}: {}", event.reservaId(), e.getMessage(), e);
        }
    }
}
