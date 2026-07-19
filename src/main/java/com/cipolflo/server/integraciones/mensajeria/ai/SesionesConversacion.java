package com.cipolflo.server.integraciones.mensajeria.ai;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vencimiento por inactividad de la memoria conversacional. {@link org.springframework.ai.chat.memory.ChatMemory}
 * no trae TTL de fábrica, así que este componente lo agrega: guarda el instante del último
 * mensaje por conversación y, si pasó el TTL configurado, limpia el historial de esa
 * conversación antes de que el próximo mensaje se agregue a ella. La clave es el
 * {@code conversacionId} (el {@code chatId} de Telegram), así que el TTL es independiente
 * por chat.
 */
@Component
public class SesionesConversacion {

    private final Map<String, Instant> ultimaActividad = new ConcurrentHashMap<>();

    private final ChatMemory chatMemory;
    private final Duration ttlInactividad;
    private final Clock clock;

    public SesionesConversacion(ChatMemory chatMemory, MemoriaConversacionalProperties properties, Clock clock) {
        this.chatMemory = chatMemory;
        this.ttlInactividad = properties.ttlInactividad();
        this.clock = clock;
    }

    /**
     * Registra actividad en {@code conversacionId}. Si pasó el TTL de inactividad desde el
     * último mensaje, limpia la memoria de esa conversación antes de registrar la actividad
     * nueva. Se llama antes de invocar al modelo.
     */
    public void registrarActividad(String conversacionId) {
        Instant ahora = clock.instant();
        Instant ultima = ultimaActividad.get(conversacionId);
        if (ultima != null && Duration.between(ultima, ahora).compareTo(ttlInactividad) > 0) {
            chatMemory.clear(conversacionId);
        }
        ultimaActividad.put(conversacionId, ahora);
    }
}
