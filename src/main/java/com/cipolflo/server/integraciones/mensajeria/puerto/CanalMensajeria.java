package com.cipolflo.server.integraciones.mensajeria.puerto;

import com.cipolflo.server.integraciones.mensajeria.log.TipoEventoMensaje;

/**
 * Puerto de salida para enviar mensajes por un canal de mensajería. El núcleo
 * ({@code AsistenteConsultas}, {@code NotificacionService}, los schedulers) depende
 * solo de esta interfaz, nunca de un canal concreto.
 *
 * {@code destinatarioId} es {@code String} a propósito: en el adaptador de Telegram es
 * el {@code chatId}; en un futuro adaptador de WhatsApp sería un número de teléfono. El
 * núcleo no necesita saber cuál.
 */
public interface CanalMensajeria {

    /**
     * Envía {@code texto} al destinatario. La implementación es responsable de sus
     * propios reintentos y de dejar traza del envío en {@code envio_mensajes_log}
     * (por eso recibe {@code tipoEvento}: es quien conoce el resultado del envío y
     * arma el registro); no lanza hacia el núcleo salvo que agote esos reintentos.
     */
    void enviar(String destinatarioId, String texto, TipoEventoMensaje tipoEvento);
}
