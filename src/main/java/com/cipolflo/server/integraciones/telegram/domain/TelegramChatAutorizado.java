package com.cipolflo.server.integraciones.telegram.domain;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Chat de Telegram habilitado a usar el bot. Corresponde a personal de la asociación (2 o 3
 * personas), no a clientes del sistema — por eso no lleva vínculo a {@code Cliente}.
 * El alta, modificación, habilitación y baja se administran desde la pantalla de Ajustes
 * (módulo {@code ajustes}), que compone este repositorio en lugar de duplicar la tabla.
 */
@Entity
@Table(name = "telegram_chat_autorizado")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TelegramChatAutorizado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long chatId;

    /** Nombre de la persona de la asociación, para saber de quién es el chat sin depender de Telegram. */
    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private Boolean activo = true;

    /** Permite tener a alguien habilitado para consultar pero sin recibir el push de las tareas programadas. */
    @Column(nullable = false)
    private Boolean recibeNotificaciones = true;

    public static TelegramChatAutorizado registrar(Long chatId, String alias, Boolean recibeNotificaciones) {
        TelegramChatAutorizado chat = new TelegramChatAutorizado();
        chat.chatId = chatId;
        chat.alias = alias;
        chat.activo = true;
        chat.recibeNotificaciones = recibeNotificaciones != null ? recibeNotificaciones : true;
        return chat;
    }

    public void modificar(String alias, Boolean recibeNotificaciones) {
        this.alias = alias;
        this.recibeNotificaciones = recibeNotificaciones;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }
}
