package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.ReservaFinalizacionRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FinalizacionReservaValidationContext {

    private final ReservaFinalizacionRequestDto dto;

    private final EstadoReserva estadoReserva;

    private final boolean reservaPaga;
}