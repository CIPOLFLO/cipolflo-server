package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;

public interface IClienteService {

    PageResponse<ListadoClientesResponseDto> getListadoClientes(ListadoClientesRequestDto filtros, PageRequestDto pageRequest);

    ClienteResponseDto getDetalleCliente(Long id);
}
