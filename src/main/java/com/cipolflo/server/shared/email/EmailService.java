package com.cipolflo.server.shared.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final EnvioEmailLogRegistrar logRegistrar;

    public EmailService(JavaMailSender mailSender, MailProperties mailProperties, EnvioEmailLogRegistrar logRegistrar) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.logRegistrar = logRegistrar;
    }

    @Override
    public void enviar(SolicitudEmail solicitud) {
        try {
            MimeMessage mensaje = construir(solicitud);
            mailSender.send(mensaje);
            logRegistrar.registrar(solicitud, EstadoEnvioEmail.ENVIADO, null);
        } catch (EmailException e) {
            registrarFallo(solicitud, e);
            throw e;
        } catch (MailException e) {
            registrarFallo(solicitud, e);
            throw new EmailException("No se pudo enviar el email a " + solicitud.destinatario(), e);
        }
    }

    private MimeMessage construir(SolicitudEmail solicitud) {
        boolean multipart = solicitud.adjuntos() != null && !solicitud.adjuntos().isEmpty();
        MimeMessage mensaje = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, multipart, StandardCharsets.UTF_8.name());
            aplicarRemitente(helper);
            helper.setTo(solicitud.destinatario());
            helper.setSubject(solicitud.asunto());
            helper.setText(solicitud.cuerpo(), solicitud.html());
            if (multipart) {
                for (EmailAdjunto adjunto : solicitud.adjuntos()) {
                    helper.addAttachment(
                            adjunto.nombre(),
                            new ByteArrayResource(adjunto.contenido()),
                            adjunto.contentType()
                    );
                }
            }
            return mensaje;
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new EmailException("No se pudo construir el email para " + solicitud.destinatario(), e);
        }
    }

    private void aplicarRemitente(MimeMessageHelper helper) throws MessagingException, UnsupportedEncodingException {
        String from = mailProperties.from();
        String nombre = mailProperties.nombreRemitente();
        if (nombre != null && !nombre.isBlank()) {
            helper.setFrom(from, nombre);
        } else {
            helper.setFrom(from);
        }
    }

    /**
     * Registra el fallo dejando en la tabla sólo una categoría no sensible (el nombre de la
     * excepción) y volcando el detalle técnico completo —que puede incluir host/puerto o
     * respuestas crudas del SMTP— únicamente en los logs de aplicación.
     */
    private void registrarFallo(SolicitudEmail solicitud, Throwable e) {
        String categoria = e.getClass().getSimpleName();
        log.error("Fallo al enviar email (evento={}, referenciaId={}): {}",
                solicitud.tipoEvento(), solicitud.referenciaId(), e.getMessage(), e);
        logRegistrar.registrar(solicitud, EstadoEnvioEmail.FALLIDO, categoria);
    }
}
