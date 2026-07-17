package com.cipolflo.server.integraciones.mensajeria.ai;

import com.cipolflo.server.integraciones.mensajeria.ai.tools.CuotaTools;
import com.cipolflo.server.integraciones.mensajeria.ai.tools.DisponibilidadTools;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuración del asistente de IA: el {@link ChatClient} (system prompt, tools, memoria)
 * y sus dependencias.
 *
 * <p><b>Restricción de diseño — memoria en RAM:</b> {@link ChatMemory} usa
 * {@link InMemoryChatMemoryRepository}, así que un reinicio o un deploy borra las
 * conversaciones en curso (aceptable: son consultas efímeras de 2-3 personas). Esto
 * <b>solo es viable con una única instancia</b> detrás del ALB. Si en algún momento se
 * escala a más de una instancia, los mensajes de un mismo chat pueden caer en instancias
 * distintas con historiales parciales — hay que migrar a {@code JdbcChatMemoryRepository}
 * (persistido en PostgreSQL) antes de escalar.
 */
@Configuration
public class AsistenteConfig {

    static final String SYSTEM_PROMPT = """
            Sos el asistente virtual de la asociación CIPOLFLO. Respondés en castellano \
            rioplatense, de forma clara y amable.

            Hoy es {fecha} (formato yyyy-MM-dd). Usalo para resolver fechas relativas como \
            "el finde que viene" o "mañana".

            Reglas estrictas:
            - Nunca inventes datos que no vengan de una tool. Si no podés resolver la consulta \
            con las tools disponibles, decilo explícitamente en vez de completar con información \
            inventada.
            - Cuando una tool devuelva varias coincidencias, transcribilas TODAS en tu respuesta. \
            No resumas a una sola ni elijas la que "parece" la correcta.
            - Si el mensaje del usuario no se corresponde con ninguna consulta que puedas resolver, \
            respondé amablemente explicando qué podés consultar: disponibilidad de servicios y \
            estado de cuota de socios.
            """;

    @Bean
    public Clock relojAsistente() {
        return Clock.system(ZonaHoraria.URUGUAY);
    }

    @Bean
    public ChatMemory chatMemory(MemoriaConversacionalProperties properties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(properties.maxMensajes())
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
                                 DisponibilidadTools disponibilidadTools, CuotaTools cuotaTools) {
        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(disponibilidadTools, cuotaTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
