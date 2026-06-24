package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;

public record ClienteDetalleReservaDto(
        Long id,
        String nombre,
        String cedula,
        String telefono,
        String email,
        TipoCliente tipoCliente
) {}
