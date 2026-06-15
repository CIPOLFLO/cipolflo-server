package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.dto.*;

import java.util.Collection;
import java.util.Map;
import com.cipolflo.server.clientes.dto.BusquedaCedulaResponseDto;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.clientes.dto.RegistroParticularRequestDto;

public interface IClienteService {

    PageResponse<ListadoClientesResponseDto> getListadoClientes(ListadoClientesRequestDto filtros, PageRequestDto pageRequest);

    ClienteResponseDto getDetalleCliente(Long id);

    Map<Long, String> getNombresByIds(Collection<Long> ids);

    void darDeBajaSocio(Long id);

    ClienteResponseDto modificarParticular(Long id, ModificacionParticularRequestDto dto);

    ClienteResponseDto modificarSocio(Long id, ModificacionSocioRequestDto dto);

    ClienteResponseDto registrarSocio(RegistroSocioRequestDto dto);

    BusquedaCedulaResponseDto buscarPorCedula(String cedula);

    ClienteResponseDto registrarParticular(RegistroParticularRequestDto dto);

}
