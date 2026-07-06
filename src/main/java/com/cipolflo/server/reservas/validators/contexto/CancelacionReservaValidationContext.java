package com.cipolflo.server.reservas.validators.contexto;


import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.dto.PagoAsociadoReservaDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CancelacionReservaValidationContext {

    private final ReservaCancelacionRequestDto dto;

    private final Reserva reserva;

    private final List<PagoAsociadoReservaDto> pagosAsociados;

    private final BigDecimal importeTotalPagos;

}
