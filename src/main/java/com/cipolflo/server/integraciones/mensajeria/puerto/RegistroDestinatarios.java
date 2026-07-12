package com.cipolflo.server.integraciones.mensajeria.puerto;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para resolver a quién se le puede hablar por un canal de mensajería.
 * Lo implementa el adaptador (en Telegram, sobre {@code telegram_chat_autorizado}); el
 * núcleo solo conoce esta interfaz.
 */
public interface RegistroDestinatarios {

    /**
     * Busca el destinatario autorizado a usar el bot (personal del club, no clientes).
     *
     * @return vacío si el {@code destinatarioId} no está registrado o no está activo
     */
    Optional<DestinatarioMensajeria> buscarAutorizado(String destinatarioId);

    /**
     * Destinatarios activos que además tienen habilitadas las notificaciones salientes
     * (reporte semanal, avisos de cuotas atrasadas, etc.).
     */
    List<DestinatarioMensajeria> destinatariosDeNotificaciones();
}
