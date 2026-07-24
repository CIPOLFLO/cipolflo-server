package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.AntiguedadReservasRequestDto;
import com.cipolflo.server.ajustes.dto.AntiguedadReservasResponseDto;

public interface IAntiguedadReservasService {

    AntiguedadReservasResponseDto obtenerAntiguedad();

    AntiguedadReservasResponseDto actualizarAntiguedad(AntiguedadReservasRequestDto dto);
}
