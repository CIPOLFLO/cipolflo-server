package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.dto.RegistroPagoReservaRequestDto;

public interface IPagoReservaService {
    void registrarPago(Long reservaId, RegistroPagoReservaRequestDto dto);
}
