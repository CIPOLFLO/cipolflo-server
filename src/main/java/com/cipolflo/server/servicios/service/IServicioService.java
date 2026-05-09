package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.dto.ListadoServiciosRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;

public interface IServicioService {
    ServicioResponseDto getDetalleServicio(Long id);

    PageResponse<ListadoServiciosResponseDto> getListadoServicios(ListadoServiciosRequestDto filtros, PageRequestDto pageRequest);
}
