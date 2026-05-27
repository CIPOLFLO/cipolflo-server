package com.cipolflo.server.clientes.mapper;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;

public class ClienteMapper {

    private ClienteMapper() {}

    public static ListadoClientesResponseDto toListadoResponseDto(Cliente cliente) {
        Socio socio = cliente instanceof Socio s ? s : null;
        return new ListadoClientesResponseDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getCedula(),
                socio != null ? TipoCliente.SOCIO : TipoCliente.PARTICULAR,
                socio != null ? socio.getNumeroSocio() : null,
                socio != null ? socio.getEstado() : null
        );
    }
}
