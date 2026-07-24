package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.ClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.HabilitacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.ListadoClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoClientesTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.ModificacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.RegistroClienteTelegramRequestDto;
import com.cipolflo.server.integraciones.telegram.domain.TelegramChatAutorizado;
import com.cipolflo.server.integraciones.telegram.exception.TelegramChatNoEncontradoException;
import com.cipolflo.server.integraciones.telegram.exception.TelegramValidacionException;
import com.cipolflo.server.integraciones.telegram.repository.TelegramChatAutorizadoRepository;
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
class ClienteTelegramServiceTest {

    @Mock
    private TelegramChatAutorizadoRepository telegramChatAutorizadoRepository;

    @InjectMocks
    private ClienteTelegramService clienteTelegramService;

    private PageRequestDto pageRequest() {
        return new PageRequestDto(0, 10, null, null);
    }

    @Test
    void getListado_sinFiltros_devuelveLaPaginaMapeada() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(123L, "Juan", true);
        Page<TelegramChatAutorizado> pagina = new PageImpl<>(List.of(chat));
        when(telegramChatAutorizadoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pagina);

        PageResponse<ListadoClienteTelegramResponseDto> response = clienteTelegramService.getListado(
                new ListadoClientesTelegramRequestDto(null, null), pageRequest());

        assertEquals(1, response.content().size());
        assertEquals(123L, response.content().get(0).chatId());
    }

    @Test
    void getDetalle_conIdExistente_devuelveElDetalle() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(123L, "Juan", true);
        when(telegramChatAutorizadoRepository.findById(1L)).thenReturn(Optional.of(chat));

        ClienteTelegramResponseDto response = clienteTelegramService.getDetalle(1L);

        assertEquals("Juan", response.alias());
    }

    @Test
    void getDetalle_conIdInexistente_lanzaChatNoEncontrado() {
        when(telegramChatAutorizadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TelegramChatNoEncontradoException.class, () -> clienteTelegramService.getDetalle(99L));
    }

    @Test
    void registrar_conDatosValidos_creaElCliente() {
        when(telegramChatAutorizadoRepository.saveAndFlush(any(TelegramChatAutorizado.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ClienteTelegramResponseDto response = clienteTelegramService.registrar(
                new RegistroClienteTelegramRequestDto(123L, "Juan", true));

        assertEquals(123L, response.chatId());
        assertEquals("Juan", response.alias());
        assertTrue(response.activo());
    }

    @Test
    void registrar_conChatIdDuplicado_lanzaTelegramValidacionException() {
        when(telegramChatAutorizadoRepository.saveAndFlush(any(TelegramChatAutorizado.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(TelegramValidacionException.class, () -> clienteTelegramService.registrar(
                new RegistroClienteTelegramRequestDto(123L, "Juan", true)));
    }

    @Test
    void modificar_actualizaAliasYRecibeNotificaciones_sinTocarElChatId() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(123L, "Juan", true);
        when(telegramChatAutorizadoRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramChatAutorizadoRepository.save(any(TelegramChatAutorizado.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ClienteTelegramResponseDto response = clienteTelegramService.modificar(
                1L, new ModificacionClienteTelegramRequestDto("Juan Pérez", false));

        assertEquals("Juan Pérez", response.alias());
        assertFalse(response.recibeNotificaciones());
        assertEquals(123L, response.chatId());
    }

    @Test
    void cambiarHabilitacion_conActivoFalse_desactivaElCliente() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(123L, "Juan", true);
        when(telegramChatAutorizadoRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramChatAutorizadoRepository.save(any(TelegramChatAutorizado.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ClienteTelegramResponseDto response = clienteTelegramService.cambiarHabilitacion(
                1L, new HabilitacionClienteTelegramRequestDto(false));

        assertFalse(response.activo());
    }

    @Test
    void cambiarHabilitacion_conActivoTrue_activaElCliente() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(123L, "Juan", true);
        chat.desactivar();
        when(telegramChatAutorizadoRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramChatAutorizadoRepository.save(any(TelegramChatAutorizado.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ClienteTelegramResponseDto response = clienteTelegramService.cambiarHabilitacion(
                1L, new HabilitacionClienteTelegramRequestDto(true));

        assertTrue(response.activo());
    }

    @Test
    void eliminar_conIdExistente_borraElRegistro() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(123L, "Juan", true);
        when(telegramChatAutorizadoRepository.findById(1L)).thenReturn(Optional.of(chat));

        clienteTelegramService.eliminar(1L);

        verify(telegramChatAutorizadoRepository).delete(chat);
    }

    @Test
    void eliminar_conIdInexistente_lanzaChatNoEncontrado() {
        when(telegramChatAutorizadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TelegramChatNoEncontradoException.class, () -> clienteTelegramService.eliminar(99L));
    }
}
