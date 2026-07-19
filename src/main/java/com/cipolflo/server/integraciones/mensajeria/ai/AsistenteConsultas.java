package com.cipolflo.server.integraciones.mensajeria.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Envuelve al {@link ChatClient} con la memoria conversacional y el manejo de errores del
 * proveedor de IA. No conoce Telegram, HTTP ni el formato del update: recibe texto, devuelve
 * texto. El {@code conversacionId} es opaco acá — en el adaptador de Telegram es el
 * {@code chatId}.
 */
@Component
public class AsistenteConsultas {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final SesionesConversacion sesionesConversacion;
    private final Clock clock;

    public AsistenteConsultas(ChatClient chatClient, ChatMemory chatMemory,
                              SesionesConversacion sesionesConversacion, Clock clock) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.sesionesConversacion = sesionesConversacion;
        this.clock = clock;
    }

    /**
     * Resuelve {@code textoUsuario} contra el modelo, con memoria por conversación. Si pasó
     * el TTL de inactividad, {@link SesionesConversacion} limpia el historial antes de esta
     * llamada.
     *
     * @throws AsistenteException si falla el proveedor de IA
     */
    public String responder(String conversacionId, String textoUsuario) {
        sesionesConversacion.registrarActividad(conversacionId);
        try {
            return chatClient.prompt()
                    .system(s -> s.param("fecha", LocalDate.now(clock).format(FORMATO_FECHA)))
                    .user(textoUsuario)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversacionId))
                    .call()
                    .content();
        } catch (RuntimeException e) {
            throw new AsistenteException("Fallo al consultar el proveedor de IA", e);
        }
    }

    /** Limpia el historial de la conversación. Usado por el comando {@code /reset}. */
    public void reiniciarConversacion(String conversacionId) {
        chatMemory.clear(conversacionId);
    }
}
