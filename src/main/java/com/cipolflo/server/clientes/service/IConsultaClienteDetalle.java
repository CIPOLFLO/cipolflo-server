package com.cipolflo.server.clientes.service;

import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;

public interface IConsultaClienteDetalle {

    ClienteDetalleReservaDto getDetallClienteSimple(Long id);
}
