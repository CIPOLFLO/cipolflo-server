package com.cipolflo.server.ajustes.mapper;

import com.cipolflo.server.ajustes.dto.DestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.shared.email.domain.DestinatarioNotificacionEmail;

public class DestinatarioNotificacionEmailMapper {

    private DestinatarioNotificacionEmailMapper() {}

    public static DestinatarioNotificacionEmailResponseDto toResponseDto(DestinatarioNotificacionEmail destinatario) {
        return new DestinatarioNotificacionEmailResponseDto(
                destinatario.getId(),
                destinatario.getEmail(),
                destinatario.getAlias(),
                destinatario.getActivo(),
                destinatario.getCreatedAt(),
                destinatario.getUpdatedAt()
        );
    }

    public static ListadoDestinatarioNotificacionEmailResponseDto toListadoResponseDto(
            DestinatarioNotificacionEmail destinatario) {
        return new ListadoDestinatarioNotificacionEmailResponseDto(
                destinatario.getId(),
                destinatario.getEmail(),
                destinatario.getAlias(),
                destinatario.getActivo(),
                destinatario.getCreatedAt(),
                destinatario.getUpdatedAt()
        );
    }
}
