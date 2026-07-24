package com.cipolflo.server.ajustes.mapper;

import com.cipolflo.server.ajustes.dto.ClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoClienteTelegramResponseDto;
import com.cipolflo.server.integraciones.telegram.domain.TelegramChatAutorizado;

public class ClienteTelegramMapper {

    private ClienteTelegramMapper() {}

    public static ClienteTelegramResponseDto toResponseDto(TelegramChatAutorizado chat) {
        return new ClienteTelegramResponseDto(
                chat.getId(),
                chat.getChatId(),
                chat.getAlias(),
                chat.getActivo(),
                chat.getRecibeNotificaciones(),
                chat.getCreatedAt(),
                chat.getUpdatedAt()
        );
    }

    public static ListadoClienteTelegramResponseDto toListadoResponseDto(TelegramChatAutorizado chat) {
        return new ListadoClienteTelegramResponseDto(
                chat.getId(),
                chat.getChatId(),
                chat.getAlias(),
                chat.getActivo(),
                chat.getRecibeNotificaciones(),
                chat.getCreatedAt(),
                chat.getUpdatedAt()
        );
    }
}
