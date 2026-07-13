package com.cipolflo.server.integraciones.telegram.processor;

import com.cipolflo.server.integraciones.mensajeria.ai.AsistenteConsultas;
import com.cipolflo.server.integraciones.mensajeria.ai.AsistenteException;
import com.cipolflo.server.integraciones.mensajeria.log.TipoEventoMensaje;
import com.cipolflo.server.integraciones.mensajeria.puerto.CanalMensajeria;
import com.cipolflo.server.integraciones.mensajeria.puerto.DestinatarioMensajeria;
import com.cipolflo.server.integraciones.mensajeria.puerto.RegistroDestinatarios;
import com.cipolflo.server.integraciones.telegram.dto.TelegramChatDto;
import com.cipolflo.server.integraciones.telegram.dto.TelegramMessageDto;
import com.cipolflo.server.integraciones.telegram.dto.TelegramUpdateDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcesadorMensajeTelegramTest {

    @Mock
    private RegistroDestinatarios registroDestinatarios;

    @Mock
    private AsistenteConsultas asistenteConsultas;

    @Mock
    private CanalMensajeria canalMensajeria;

    private ProcesadorMensajeTelegram nuevoProcesador() {
        return new ProcesadorMensajeTelegram(registroDestinatarios, asistenteConsultas, canalMensajeria);
    }

    private TelegramUpdateDto updateConTexto(Long chatId, String username, String texto) {
        return new TelegramUpdateDto(1L, new TelegramMessageDto(texto, new TelegramChatDto(chatId, username)));
    }

    @Test
    void updateSinMensajeDeTexto_deberiaIgnorarse() {
        TelegramUpdateDto update = new TelegramUpdateDto(1L, null);

        nuevoProcesador().procesar(update);

        verify(registroDestinatarios, never()).buscarAutorizado(anyString());
    }

    @Test
    void updateConTextoVacio_deberiaIgnorarse() {
        TelegramUpdateDto update = updateConTexto(123L, "user", "  ");

        nuevoProcesador().procesar(update);

        verify(registroDestinatarios, never()).buscarAutorizado(anyString());
    }

    @Test
    void chatNoAutorizado_noDeberiaLlamarAlAsistenteYDeberiaEnviarRechazo() {
        when(registroDestinatarios.buscarAutorizado("123")).thenReturn(Optional.empty());
        TelegramUpdateDto update = updateConTexto(123L, "intruso", "hola");

        nuevoProcesador().procesar(update);

        verify(asistenteConsultas, never()).responder(anyString(), anyString());
        verify(canalMensajeria).enviar(eq("123"), anyString(), eq(TipoEventoMensaje.RECHAZO_NO_AUTORIZADO));
    }

    @Test
    void chatAutorizado_deberiaInvocarAlAsistenteConElChatIdYMandarLaRespuesta() {
        when(registroDestinatarios.buscarAutorizado("123"))
                .thenReturn(Optional.of(new DestinatarioMensajeria("123", "Camila")));
        when(asistenteConsultas.responder("123", "hola")).thenReturn("respuesta del bot");
        TelegramUpdateDto update = updateConTexto(123L, "camila", "hola");

        nuevoProcesador().procesar(update);

        verify(asistenteConsultas).responder("123", "hola");
        verify(canalMensajeria).enviar("123", "respuesta del bot", TipoEventoMensaje.RESPUESTA_CONSULTA);
    }

    @Test
    void comandoReset_deberiaReiniciarConversacionYConfirmarSinLlamarAlModelo() {
        when(registroDestinatarios.buscarAutorizado("123"))
                .thenReturn(Optional.of(new DestinatarioMensajeria("123", "Camila")));
        TelegramUpdateDto update = updateConTexto(123L, "camila", "/reset");

        nuevoProcesador().procesar(update);

        verify(asistenteConsultas).reiniciarConversacion("123");
        verify(asistenteConsultas, never()).responder(anyString(), anyString());
        verify(canalMensajeria).enviar(eq("123"), anyString(), eq(TipoEventoMensaje.RESPUESTA_CONSULTA));
    }

    @Test
    void fallaDelAsistente_deberiaEnviarMensajeGenericoDeError() {
        when(registroDestinatarios.buscarAutorizado("123"))
                .thenReturn(Optional.of(new DestinatarioMensajeria("123", "Camila")));
        when(asistenteConsultas.responder("123", "hola")).thenThrow(new AsistenteException("boom", null));
        TelegramUpdateDto update = updateConTexto(123L, "camila", "hola");

        nuevoProcesador().procesar(update);

        verify(canalMensajeria).enviar(eq("123"), anyString(), eq(TipoEventoMensaje.RESPUESTA_CONSULTA));
    }

    @Test
    void fallaDelCanalAlEnviar_noDeberiaPropagarNiReintentarConUnAvisoAparte() {
        when(registroDestinatarios.buscarAutorizado("123"))
                .thenReturn(Optional.of(new DestinatarioMensajeria("123", "Camila")));
        when(asistenteConsultas.responder("123", "hola")).thenReturn("respuesta");
        doThrow(new RuntimeException("Telegram caído"))
                .when(canalMensajeria).enviar(eq("123"), anyString(), any());
        TelegramUpdateDto update = updateConTexto(123L, "camila", "hola");

        nuevoProcesador().procesar(update);

        verify(canalMensajeria, times(1)).enviar(eq("123"), anyString(), any());
    }
}
