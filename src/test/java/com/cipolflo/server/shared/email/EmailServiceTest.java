package com.cipolflo.server.shared.email;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EnvioEmailLogRegistrar logRegistrar;

    private EmailService emailServiceCon(MailProperties props) {
        return new EmailService(mailSender, props, logRegistrar);
    }

    private MailProperties propsConNombre() {
        return new MailProperties("from@cipolflo.com", "CIPOLFLO");
    }

    private SolicitudEmail solicitudTexto() {
        return SolicitudEmail.texto("cliente@mail.com", "Asunto", "Cuerpo", TipoEventoEmail.RESERVA_CREADA, 1L);
    }

    @Test
    void envioExitoso_enviaYRegistraEnviado() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        EmailService emailService = emailServiceCon(propsConNombre());
        SolicitudEmail solicitud = solicitudTexto();

        emailService.enviar(solicitud);

        verify(mailSender).send(any(MimeMessage.class));
        verify(logRegistrar).registrar(eq(solicitud), eq(EstadoEnvioEmail.ENVIADO), isNull());
    }

    @Test
    void envioConAdjunto_seEnvia() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        EmailService emailService = emailServiceCon(propsConNombre());
        SolicitudEmail solicitud = SolicitudEmail.conAdjuntos(
                "cliente@mail.com", "Asunto", "Cuerpo", false,
                List.of(new EmailAdjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf")),
                TipoEventoEmail.RESERVA_CREADA, 1L);

        emailService.enviar(solicitud);

        verify(mailSender).send(any(MimeMessage.class));
        verify(logRegistrar).registrar(eq(solicitud), eq(EstadoEnvioEmail.ENVIADO), isNull());
    }

    @Test
    void envioConVariosDestinatariosSeparadosPorComa_seEnviaATodos() throws Exception {
        MimeMessage mensaje = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mensaje);
        EmailService emailService = emailServiceCon(propsConNombre());
        SolicitudEmail solicitud = SolicitudEmail.texto(
                "uno@mail.com, dos@mail.com", "Asunto", "Cuerpo", TipoEventoEmail.REPORTE_SEMANAL_RESERVAS, null);

        emailService.enviar(solicitud);

        assertEquals(2, mensaje.getAllRecipients().length);
        verify(mailSender).send(any(MimeMessage.class));
        verify(logRegistrar).registrar(eq(solicitud), eq(EstadoEnvioEmail.ENVIADO), isNull());
    }

    @Test
    void remitenteSinNombre_seEnviaIgual() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        EmailService emailService = emailServiceCon(new MailProperties("from@cipolflo.com", null));

        emailService.enviar(solicitudTexto());

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void envioFalla_registraCategoriaSanitizadaSinElMensajeCrudoYLanzaEmailException() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doThrow(new MailSendException("Couldn't connect to host, port: smtp.gmail.com, 587"))
                .when(mailSender).send(any(MimeMessage.class));
        EmailService emailService = emailServiceCon(propsConNombre());
        SolicitudEmail solicitud = solicitudTexto();

        assertThrows(EmailException.class, () -> emailService.enviar(solicitud));

        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        verify(logRegistrar).registrar(eq(solicitud), eq(EstadoEnvioEmail.FALLIDO), errorCaptor.capture());
        // Sólo la categoría (nombre de la excepción); nunca el detalle SMTP con host/puerto.
        assertEquals("MailSendException", errorCaptor.getValue());
        assertFalse(errorCaptor.getValue().contains("smtp.gmail.com"));
    }

    @Test
    void fallaAlConstruir_registraFallidoYLanzaEmailExceptionSinEnviar() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        EmailService emailService = emailServiceCon(propsConNombre());
        // Destinatario inválido: MimeMessageHelper.setTo falla al parsear la dirección.
        SolicitudEmail solicitud = SolicitudEmail.texto(
                "destinatario invalido", "Asunto", "Cuerpo", TipoEventoEmail.RESERVA_CREADA, 1L);

        assertThrows(EmailException.class, () -> emailService.enviar(solicitud));

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(logRegistrar).registrar(eq(solicitud), eq(EstadoEnvioEmail.FALLIDO), eq("EmailException"));
    }
}
