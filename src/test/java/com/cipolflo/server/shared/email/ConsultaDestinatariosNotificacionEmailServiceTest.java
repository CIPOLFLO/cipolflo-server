package com.cipolflo.server.shared.email;

import com.cipolflo.server.shared.email.domain.DestinatarioNotificacionEmail;
import com.cipolflo.server.shared.email.repository.DestinatarioNotificacionEmailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaDestinatariosNotificacionEmailServiceTest {

    @Mock
    private DestinatarioNotificacionEmailRepository repository;

    @InjectMocks
    private ConsultaDestinatariosNotificacionEmailService service;

    @Test
    void destinatariosActivos_sinDestinatarios_devuelveNull() {
        when(repository.findByActivoTrue()).thenReturn(List.of());

        assertNull(service.destinatariosActivos());
    }

    @Test
    void destinatariosActivos_conUnDestinatario_devuelveElEmail() {
        when(repository.findByActivoTrue())
                .thenReturn(List.of(DestinatarioNotificacionEmail.registrar("admin@cipolflo.com", "Administración")));

        assertEquals("admin@cipolflo.com", service.destinatariosActivos());
    }

    @Test
    void destinatariosActivos_conVariosDestinatarios_losUneConComas() {
        when(repository.findByActivoTrue()).thenReturn(List.of(
                DestinatarioNotificacionEmail.registrar("admin@cipolflo.com", "Administración"),
                DestinatarioNotificacionEmail.registrar("tesoreria@cipolflo.com", "Tesorería")));

        assertEquals("admin@cipolflo.com,tesoreria@cipolflo.com", service.destinatariosActivos());
    }
}
