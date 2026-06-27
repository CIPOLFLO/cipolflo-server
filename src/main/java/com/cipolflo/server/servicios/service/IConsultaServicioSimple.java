package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;

import java.util.Collection;
import java.util.Map;

public interface IConsultaServicioSimple {
    ServicioDetalleReservaDto getDetalleServicioSimple(Long id);

    Map<Long, String> getNombresByIds(Collection<Long> ids);
}
