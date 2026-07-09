package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.dto.ReservaFinalizacionCheckResponseDto;
import com.cipolflo.server.reservas.dto.ReservaFinalizacionRequestDto;

public interface IFinalizacionReservaService {

    ReservaFinalizacionCheckResponseDto verificarFinalizacion(Long reservaId);

    void finalizar(Long reservaId, ReservaFinalizacionRequestDto dto);
}
