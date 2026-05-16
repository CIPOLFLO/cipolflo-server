package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.dto.ListadoServiciosRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.servicios.dto.ModificacionServicioDto;
import com.cipolflo.server.servicios.dto.ReservaProximaResponseDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;

import java.util.List;

public interface IServicioService {
    ServicioResponseDto getDetalleServicio(Long id);

    ServicioResponseDto cambiarHabilitacionServicio(Long id, ServicioRequestDto request);

    List<ReservaProximaResponseDto> getReservasProximas(Long id);

    PageResponse<ListadoServiciosResponseDto> getListadoServicios(ListadoServiciosRequestDto filtros, PageRequestDto pageRequest);

    ServicioResponseDto modificarServicio(Long id, ModificacionServicioDto request);
}
