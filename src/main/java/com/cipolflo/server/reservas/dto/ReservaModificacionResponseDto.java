package com.cipolflo.server.reservas.dto;

import lombok.Getter;

@Getter
public class ReservaModificacionResponseDto {

    private final Long id;

    public ReservaModificacionResponseDto(Long id) {
        this.id = id;
    }
}
