package com.cipolflo.server.clientes.service;

import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IConsultaClienteDetalle {

    ClienteDetalleReservaDto getDetallClienteSimple(Long id);

    List<Long> getIdsByNombre(String nombre);

    Map<Long, String> getNombresByIds(Collection<Long> ids);
}
