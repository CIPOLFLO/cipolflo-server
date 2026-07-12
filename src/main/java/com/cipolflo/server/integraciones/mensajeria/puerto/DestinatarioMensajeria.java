package com.cipolflo.server.integraciones.mensajeria.puerto;

/**
 * Destinatario de un canal de mensajería, visto desde el núcleo agnóstico del canal.
 * {@code destinatarioId} es el identificador que el adaptador necesita para enviar
 * (en Telegram, el {@code chatId}; en un futuro adaptador de WhatsApp, un teléfono).
 * {@code alias} es el nombre de la persona, para logs y mensajes legibles.
 */
public record DestinatarioMensajeria(String destinatarioId, String alias) {
}
