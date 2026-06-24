package com.cipolflo.server.reservas.dto;

import lombok.Getter;

@Getter
public class ReservaCreacionResponseDto {

    private final Long id;

    public ReservaCreacionResponseDto(Long id) {
        this.id = id;
    }
}
