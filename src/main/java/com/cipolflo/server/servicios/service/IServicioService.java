package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;

public interface IServicioService {
    ServicioResponseDto getDetalleServicio(Long id);
}
