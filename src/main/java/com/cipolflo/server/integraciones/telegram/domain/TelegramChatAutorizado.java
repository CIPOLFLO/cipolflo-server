package com.cipolflo.server.integraciones.telegram.domain;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chat de Telegram habilitado a usar el bot. Corresponde a personal de la asociación (2 o 3
 * personas), no a clientes del sistema — por eso no lleva vínculo a {@code Cliente}.
 * El alta se hace por script de datos, no hay endpoints de administración.
 */
@Entity
@Table(name = "telegram_chat_autorizado")
@Getter
@Setter
@NoArgsConstructor
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
}
