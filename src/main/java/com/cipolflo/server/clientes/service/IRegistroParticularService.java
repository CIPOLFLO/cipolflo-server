package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.RegistroParticularRequestDto;

public interface IRegistroParticularService {

    ClienteResponseDto registrarParticular(RegistroParticularRequestDto dto);
}
