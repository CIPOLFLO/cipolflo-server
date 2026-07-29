package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.DestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.HabilitacionDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatariosNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.ModificacionDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.RegistroDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.shared.email.domain.DestinatarioNotificacionEmail;
import com.cipolflo.server.shared.email.exception.DestinatarioNotificacionEmailNoEncontradoException;
import com.cipolflo.server.shared.email.exception.DestinatarioNotificacionEmailValidacionException;
import com.cipolflo.server.shared.email.repository.DestinatarioNotificacionEmailRepository;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DestinatarioNotificacionEmailServiceTest {

    @Mock
    private DestinatarioNotificacionEmailRepository destinatarioNotificacionEmailRepository;

    @InjectMocks
    private DestinatarioNotificacionEmailService destinatarioNotificacionEmailService;

    private PageRequestDto pageRequest() {
        return new PageRequestDto(0, 10, null, null);
    }

    @Test
    void getListado_sinFiltros_devuelveLaPaginaMapeada() {
        DestinatarioNotificacionEmail destinatario =
                DestinatarioNotificacionEmail.registrar("admin@cipolflo.com", "Administración");
        Page<DestinatarioNotificacionEmail> pagina = new PageImpl<>(List.of(destinatario));
        when(destinatarioNotificacionEmailRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pagina);

        PageResponse<ListadoDestinatarioNotificacionEmailResponseDto> response =
                destinatarioNotificacionEmailService.getListado(
                        new ListadoDestinatariosNotificacionEmailRequestDto(null, null), pageRequest());

        assertEquals(1, response.content().size());
        assertEquals("admin@cipolflo.com", response.content().get(0).email());
    }

    @Test
    void getDetalle_conIdExistente_devuelveElDetalle() {
        DestinatarioNotificacionEmail destinatario =
                DestinatarioNotificacionEmail.registrar("admin@cipolflo.com", "Administración");
        when(destinatarioNotificacionEmailRepository.findById(1L)).thenReturn(Optional.of(destinatario));

        DestinatarioNotificacionEmailResponseDto response = destinatarioNotificacionEmailService.getDetalle(1L);

        assertEquals("Administración", response.alias());
    }

    @Test
    void getDetalle_conIdInexistente_lanzaNoEncontrado() {
        when(destinatarioNotificacionEmailRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DestinatarioNotificacionEmailNoEncontradoException.class,
                () -> destinatarioNotificacionEmailService.getDetalle(99L));
    }

    @Test
    void registrar_conDatosValidos_creaElDestinatario() {
        when(destinatarioNotificacionEmailRepository.saveAndFlush(any(DestinatarioNotificacionEmail.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DestinatarioNotificacionEmailResponseDto response = destinatarioNotificacionEmailService.registrar(
                new RegistroDestinatarioNotificacionEmailRequestDto("admin@cipolflo.com", "Administración"));

        assertEquals("admin@cipolflo.com", response.email());
        assertEquals("Administración", response.alias());
        assertTrue(response.activo());
    }

    @Test
    void registrar_conEmailDuplicado_lanzaValidacionException() {
        when(destinatarioNotificacionEmailRepository.saveAndFlush(any(DestinatarioNotificacionEmail.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(DestinatarioNotificacionEmailValidacionException.class, () -> destinatarioNotificacionEmailService
                .registrar(new RegistroDestinatarioNotificacionEmailRequestDto("admin@cipolflo.com", "Administración")));
    }

    @Test
    void modificar_actualizaAliasSinTocarElEmail() {
        DestinatarioNotificacionEmail destinatario =
                DestinatarioNotificacionEmail.registrar("admin@cipolflo.com", "Administración");
        when(destinatarioNotificacionEmailRepository.findById(1L)).thenReturn(Optional.of(destinatario));
        when(destinatarioNotificacionEmailRepository.save(any(DestinatarioNotificacionEmail.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DestinatarioNotificacionEmailResponseDto response = destinatarioNotificacionEmailService.modificar(
                1L, new ModificacionDestinatarioNotificacionEmailRequestDto("Tesorería"));

        assertEquals("Tesorería", response.alias());
        assertEquals("admin@cipolflo.com", response.email());
    }

    @Test
    void cambiarHabilitacion_conActivoFalse_desactivaElDestinatario() {
        DestinatarioNotificacionEmail destinatario =
                DestinatarioNotificacionEmail.registrar("admin@cipolflo.com", "Administración");
        when(destinatarioNotificacionEmailRepository.findById(1L)).thenReturn(Optional.of(destinatario));
        when(destinatarioNotificacionEmailRepository.save(any(DestinatarioNotificacionEmail.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DestinatarioNotificacionEmailResponseDto response = destinatarioNotificacionEmailService.cambiarHabilitacion(
                1L, new HabilitacionDestinatarioNotificacionEmailRequestDto(false));

        assertFalse(response.activo());
    }

    @Test
    void eliminar_conIdExistente_borraElRegistro() {
        DestinatarioNotificacionEmail destinatario =
                DestinatarioNotificacionEmail.registrar("admin@cipolflo.com", "Administración");
        when(destinatarioNotificacionEmailRepository.findById(1L)).thenReturn(Optional.of(destinatario));

        destinatarioNotificacionEmailService.eliminar(1L);

        verify(destinatarioNotificacionEmailRepository).delete(destinatario);
    }

    @Test
    void eliminar_conIdInexistente_lanzaNoEncontrado() {
        when(destinatarioNotificacionEmailRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DestinatarioNotificacionEmailNoEncontradoException.class,
                () -> destinatarioNotificacionEmailService.eliminar(99L));
    }
}
