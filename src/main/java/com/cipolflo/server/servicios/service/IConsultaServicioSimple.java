package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;

public interface IConsultaServicioSimple {
    ServicioDetalleReservaDto getDetalleServicioSimple(Long id);
}
