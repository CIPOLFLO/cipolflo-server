package com.cipolflo.server.integraciones.telegram.repository;

import com.cipolflo.server.integraciones.mensajeria.puerto.DestinatarioMensajeria;
import com.cipolflo.server.integraciones.mensajeria.puerto.RegistroDestinatarios;
import com.cipolflo.server.integraciones.telegram.domain.TelegramChatAutorizado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Implementa {@link RegistroDestinatarios} sobre {@code telegram_chat_autorizado}. Los
 * métodos del puerto son {@code default}: adaptan el {@code chatId} numérico de Telegram
 * al {@code destinatarioId} de tipo {@code String} que espera el núcleo.
 */
public interface TelegramChatAutorizadoRepository
        extends JpaRepository<TelegramChatAutorizado, Long>, RegistroDestinatarios {

    Optional<TelegramChatAutorizado> findByChatIdAndActivoTrue(Long chatId);

    List<TelegramChatAutorizado> findByActivoTrueAndRecibeNotificacionesTrue();

    @Override
    default Optional<DestinatarioMensajeria> buscarAutorizado(String destinatarioId) {
        return findByChatIdAndActivoTrue(Long.valueOf(destinatarioId))
                .map(TelegramChatAutorizadoRepository::aDestinatario);
    }

    @Override
    default List<DestinatarioMensajeria> destinatariosDeNotificaciones() {
        return findByActivoTrueAndRecibeNotificacionesTrue().stream()
                .map(TelegramChatAutorizadoRepository::aDestinatario)
                .toList();
    }

    private static DestinatarioMensajeria aDestinatario(TelegramChatAutorizado chat) {
        return new DestinatarioMensajeria(String.valueOf(chat.getChatId()), chat.getAlias());
    }
}
