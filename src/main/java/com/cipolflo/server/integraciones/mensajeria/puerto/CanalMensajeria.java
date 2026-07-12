package com.cipolflo.server.integraciones.mensajeria.puerto;

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
     * propios reintentos y de dejar traza del envío (ver {@code envio_mensajes_log});
     * no lanza hacia el núcleo salvo que agote esos reintentos.
     */
    void enviar(String destinatarioId, String texto);
}
