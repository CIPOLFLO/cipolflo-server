package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.servicios.dto.ServicioReferenciaDto;
import com.cipolflo.server.shared.enums.Procedencia;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IConsultaServicioSimple {
    ServicioDetalleReservaDto getDetalleServicioSimple(Long id);

    Map<Long, String> getNombresByIds(Collection<Long> ids);

    /** Busca por nombre (parcial), sin filtrar por procedencia ni habilitado ni desambiguar. */
    List<ServicioReferenciaDto> buscarPorNombre(String nombre, Procedencia procedencia);
}
