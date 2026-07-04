package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.dto.ReservaCancelacionCheckResponseDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;

public interface ICancelacionReservaService {

    ReservaCancelacionCheckResponseDto verificarCancelacion(Long reservaId);

    void cancelar(Long reservaId, ReservaCancelacionRequestDto dto);
}
