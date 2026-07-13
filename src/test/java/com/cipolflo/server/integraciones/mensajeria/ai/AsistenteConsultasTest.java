package com.cipolflo.server.integraciones.mensajeria.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsistenteConsultasTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private SesionesConversacion sesionesConversacion;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private ChatClient.AdvisorSpec advisorSpec;

    private final Clock clock = Clock.fixed(Instant.parse("2026-03-10T12:00:00Z"), ZoneOffset.UTC);

    private AsistenteConsultas asistenteConsultas;

    @BeforeEach
    void setUp() {
        asistenteConsultas = new AsistenteConsultas(chatClient, chatMemory, sesionesConversacion, clock);
    }

    @SuppressWarnings("unchecked")
    private void mockearCadenaExitosa(String respuesta) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenAnswer(invocation -> {
            Consumer<ChatClient.AdvisorSpec> consumer = invocation.getArgument(0);
            consumer.accept(advisorSpec);
            return requestSpec;
        });
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(respuesta);
    }

    @Test
    void responder_deberiaDevolverElContenidoYUsarElConversacionIdCorrecto() {
        mockearCadenaExitosa("hola, ¿en qué te ayudo?");

        String resultado = asistenteConsultas.responder("chat-1", "hola");

        assertEquals("hola, ¿en qué te ayudo?", resultado);
        verify(sesionesConversacion).registrarActividad("chat-1");
        verify(advisorSpec).param(ChatMemory.CONVERSATION_ID, "chat-1");
        verify(requestSpec).user("hola");
    }

    @Test
    void reiniciarConversacion_deberiaDelegarEnChatMemoryClear() {
        asistenteConsultas.reiniciarConversacion("chat-1");

        verify(chatMemory).clear("chat-1");
    }

    @Test
    void fallaDelProveedor_deberiaLanzarAsistenteException() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("proveedor caído"));

        assertThrows(AsistenteException.class, () -> asistenteConsultas.responder("chat-1", "hola"));
    }
}
