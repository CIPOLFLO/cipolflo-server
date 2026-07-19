package com.cipolflo.server.integraciones.telegram.processor;

import com.cipolflo.server.integraciones.mensajeria.ai.AsistenteConsultas;
import com.cipolflo.server.integraciones.mensajeria.ai.AsistenteException;
import com.cipolflo.server.integraciones.mensajeria.log.TipoEventoMensaje;
import com.cipolflo.server.integraciones.mensajeria.puerto.CanalMensajeria;
import com.cipolflo.server.integraciones.mensajeria.puerto.DestinatarioMensajeria;
import com.cipolflo.server.integraciones.mensajeria.puerto.RegistroDestinatarios;
import com.cipolflo.server.integraciones.telegram.dto.TelegramMessageDto;
import com.cipolflo.server.integraciones.telegram.dto.TelegramUpdateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Decide qué hacer con cada update de Telegram. Vive en el adaptador porque conoce el
 * formato del update; delega toda la lógica de negocio en el núcleo agnóstico del canal
 * ({@link AsistenteConsultas}, {@link RegistroDestinatarios}, {@link CanalMensajeria}).
 *
 * {@code @Async("telegramExecutor")}: corre en un hilo aparte del que respondió el 200 al
 * webhook, para no tener a Telegram esperando la latencia del modelo. Ninguna excepción se
 * propaga fuera de este método: el hilo async no tiene a quién propagarle.
 */
@Component
public class ProcesadorMensajeTelegram implements IProcesadorMensajeTelegram {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorMensajeTelegram.class);

    private static final String COMANDO_RESET = "/reset";
    private static final String MENSAJE_RESET_OK = "Listo, arrancamos de cero. ¿En qué te ayudo?";
    private static final String MENSAJE_ERROR_GENERICO =
            "No pude procesar tu consulta en este momento, probá de nuevo en unos minutos.";

    private final RegistroDestinatarios registroDestinatarios;
    private final AsistenteConsultas asistenteConsultas;
    private final CanalMensajeria canalMensajeria;

    public ProcesadorMensajeTelegram(RegistroDestinatarios registroDestinatarios,
                                     AsistenteConsultas asistenteConsultas,
                                     CanalMensajeria canalMensajeria) {
        this.registroDestinatarios = registroDestinatarios;
        this.asistenteConsultas = asistenteConsultas;
        this.canalMensajeria = canalMensajeria;
    }

    @Override
    @Async("telegramExecutor")
    public void procesar(TelegramUpdateDto update) {
        TelegramMessageDto message = update.message();
        if (message == null || message.chat() == null || message.text() == null || message.text().isBlank()) {
            log.debug("Update sin mensaje de texto, se ignora: {}", update.updateId());
            return;
        }

        String chatId = String.valueOf(message.chat().id());
        String texto = message.text().trim();

        Optional<DestinatarioMensajeria> destinatario = registroDestinatarios.buscarAutorizado(chatId);
        if (destinatario.isEmpty()) {
            log.warn("Chat no autorizado intentó usar el bot: chatId={}, username={}", chatId, message.chat().username());
            String mensajeRechazo = "No tenés acceso a este bot. Tu ID es " + chatId
                    + " — pasaselo a un administrador para que te dé de alta.";
            enviarSeguro(chatId, mensajeRechazo, TipoEventoMensaje.RECHAZO_NO_AUTORIZADO);
            return;
        }

        if (COMANDO_RESET.equalsIgnoreCase(texto)) {
            asistenteConsultas.reiniciarConversacion(chatId);
            enviarSeguro(chatId, MENSAJE_RESET_OK, TipoEventoMensaje.RESPUESTA_CONSULTA);
            return;
        }

        String respuesta;
        try {
            respuesta = asistenteConsultas.responder(chatId, texto);
        } catch (AsistenteException e) {
            log.error("Falló el asistente para chatId={}: {}", chatId, e.getMessage(), e);
            enviarSeguro(chatId, MENSAJE_ERROR_GENERICO, TipoEventoMensaje.RESPUESTA_CONSULTA);
            return;
        }

        enviarSeguro(chatId, respuesta, TipoEventoMensaje.RESPUESTA_CONSULTA);
    }

    private void enviarSeguro(String chatId, String texto, TipoEventoMensaje tipoEvento) {
        try {
            canalMensajeria.enviar(chatId, texto, tipoEvento);
        } catch (RuntimeException e) {
            log.error("Falló el envío por Telegram a chatId={}: {}", chatId, e.getMessage(), e);
        }
    }
}
