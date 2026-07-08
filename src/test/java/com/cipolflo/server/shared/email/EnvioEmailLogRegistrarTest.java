package com.cipolflo.server.shared.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnvioEmailLogRegistrarTest {

    @Mock
    private EnvioEmailLogRepository repository;

    @InjectMocks
    private EnvioEmailLogRegistrar registrar;

    @Test
    void registrar_persisteLosDatosDeLaSolicitud() {
        SolicitudEmail solicitud = SolicitudEmail.texto(
                "cliente@mail.com", "Asunto", "Cuerpo", TipoEventoEmail.RESERVA_CREADA, 7L);

        registrar.registrar(solicitud, EstadoEnvioEmail.ENVIADO, null);

        ArgumentCaptor<EnvioEmailLog> captor = ArgumentCaptor.forClass(EnvioEmailLog.class);
        verify(repository).save(captor.capture());
        EnvioEmailLog log = captor.getValue();
        assertEquals("cliente@mail.com", log.getDestinatario());
        assertEquals("Asunto", log.getAsunto());
        assertEquals(TipoEventoEmail.RESERVA_CREADA, log.getTipoEvento());
        assertEquals(7L, log.getReferenciaId());
        assertEquals(EstadoEnvioEmail.ENVIADO, log.getEstado());
        assertNull(log.getError());
    }

    @Test
    void registrar_guardaElErrorCuandoFallo() {
        SolicitudEmail solicitud = SolicitudEmail.texto(
                "cliente@mail.com", "Asunto", "Cuerpo", TipoEventoEmail.RESERVA_CREADA, 7L);

        registrar.registrar(solicitud, EstadoEnvioEmail.FALLIDO, "SMTP caído");

        ArgumentCaptor<EnvioEmailLog> captor = ArgumentCaptor.forClass(EnvioEmailLog.class);
        verify(repository).save(captor.capture());
        EnvioEmailLog log = captor.getValue();
        assertEquals(EstadoEnvioEmail.FALLIDO, log.getEstado());
        assertEquals("SMTP caído", log.getError());
    }
}
