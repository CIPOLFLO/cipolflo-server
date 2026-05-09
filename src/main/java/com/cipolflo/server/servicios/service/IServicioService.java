package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.dto.ServicioHabilitacionResponseDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;

public interface IServicioService {
    ServicioResponseDto getDetalleServicio(Long id);
    ServicioHabilitacionResponseDto cambiarHabilitacionServicio(Long id, ServicioRequestDto request);
}
