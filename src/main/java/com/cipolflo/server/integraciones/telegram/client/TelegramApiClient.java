package com.cipolflo.server.integraciones.telegram.client;

import com.cipolflo.server.integraciones.mensajeria.log.CanalMensaje;
import com.cipolflo.server.integraciones.mensajeria.log.EnvioMensajeLogRegistrar;
import com.cipolflo.server.integraciones.mensajeria.log.EstadoEnvioMensaje;
import com.cipolflo.server.integraciones.mensajeria.log.TipoEventoMensaje;
import com.cipolflo.server.integraciones.mensajeria.puerto.CanalMensajeria;
import com.cipolflo.server.integraciones.telegram.exception.TelegramEnvioException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Adaptador de {@link CanalMensajeria} sobre la API de Telegram ({@code sendMessage}).
 * Reintenta ante errores 5xx o de red (posiblemente transitorios), pero no ante 4xx
 * (indican un problema nuestro: chat inexistente, bot bloqueado por el usuario, etc. —
 * reintentar no cambiaría el resultado). Todo envío, exitoso o no, queda registrado en
 * {@code envio_mensajes_log}; agotados los reintentos se lanza {@link TelegramEnvioException},
 * que los llamadores (procesador de mensajes, notificaciones) contienen y loguean.
 */
@Component
public class TelegramApiClient implements CanalMensajeria {

    private static final int MAX_REINTENTOS = 2;
    private static final Duration BACKOFF = Duration.ofMillis(200);

    private final RestClient restClient;
    private final EnvioMensajeLogRegistrar logRegistrar;

    public TelegramApiClient(RestClient.Builder restClientBuilder, TelegramProperties properties,
                             EnvioMensajeLogRegistrar logRegistrar) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.telegram.org/bot" + properties.botToken())
                .build();
        this.logRegistrar = logRegistrar;
    }

    @Override
    public void enviar(String destinatarioId, String texto, TipoEventoMensaje tipoEvento) {
        RestClientException error = null;
        for (int intento = 0; intento <= MAX_REINTENTOS; intento++) {
            try {
                enviarRequest(destinatarioId, texto);
                logRegistrar.registrar(CanalMensaje.TELEGRAM, destinatarioId, tipoEvento, EstadoEnvioMensaje.ENVIADO, null);
                return;
            } catch (HttpClientErrorException e) {
                error = e;
                break;
            } catch (RestClientException e) {
                error = e;
                if (intento < MAX_REINTENTOS) {
                    esperarAntesDeReintentar();
                }
            }
        }
        String detalle = describir(error);
        logRegistrar.registrar(CanalMensaje.TELEGRAM, destinatarioId, tipoEvento, EstadoEnvioMensaje.FALLIDO, detalle);
        throw new TelegramEnvioException("No se pudo enviar el mensaje a Telegram: " + detalle, error);
    }

    private void enviarRequest(String destinatarioId, String texto) {
        restClient.post()
                .uri("/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SendMessageRequest(Long.parseLong(destinatarioId), texto))
                .retrieve()
                .toBodilessEntity();
    }

    private void esperarAntesDeReintentar() {
        try {
            Thread.sleep(BACKOFF.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String describir(RestClientException e) {
        if (e instanceof HttpStatusCodeException statusEx) {
            return "HTTP " + statusEx.getStatusCode().value() + ": " + statusEx.getStatusText();
        }
        return e.getMessage();
    }

    private record SendMessageRequest(@JsonProperty("chat_id") long chatId, String text) {
    }
}
