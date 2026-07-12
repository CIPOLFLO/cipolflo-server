package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;

public record ClienteDetalleReservaDto(
        Long id,
        String nombre,
        String cedula,
        String rut,
        String telefono,
        String email,
        TipoCliente tipoCliente
) {

    /**
     * Documento identificatorio del cliente: RUT para las empresas, cédula para el resto.
     */
    public String documento() {
        return tipoCliente == TipoCliente.EMPRESA ? rut : cedula;
    }
}
