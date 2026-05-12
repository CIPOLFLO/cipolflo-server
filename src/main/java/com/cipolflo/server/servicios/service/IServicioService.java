package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.dto.ReservaProximaResponseDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;

import java.util.List;

public interface IServicioService {
    ServicioResponseDto getDetalleServicio(Long id);
    ServicioResponseDto cambiarHabilitacionServicio(Long id, ServicioRequestDto request);
    List<ReservaProximaResponseDto> getReservasProximas(Long id);
}
