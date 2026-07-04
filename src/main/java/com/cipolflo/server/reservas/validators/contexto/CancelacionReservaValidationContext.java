package com.cipolflo.server.reservas.validators.contexto;


import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.PagoAsociadoReservaDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CancelacionReservaValidationContext {

    private final ReservaCancelacionRequestDto dto;

    private final EstadoReserva estadoReserva;

    private final List<PagoAsociadoReservaDto> pagosAsociados;

}