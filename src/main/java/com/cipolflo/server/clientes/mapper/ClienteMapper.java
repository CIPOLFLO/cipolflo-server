package com.cipolflo.server.clientes.mapper;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.*;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;

public class ClienteMapper {

    private ClienteMapper() {}

    public static ClienteResponseDto toDetalleResponseDto(Cliente cliente, UltimaCuotaDto ultimaCuotaDto) {
        Socio socio = cliente instanceof Socio s ? s : null;
        return new ClienteResponseDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getCedula(),
                socio != null ? socio.getFechaNacimiento() : null,
                cliente.getTelefono(),
                cliente.getMail(),
                socio != null ? socio.getMetodoCobro() : null,
                socio != null ? socio.getPais() : null,
                socio != null ? socio.getDepartamento() : null,
                socio != null ? socio.getCiudad() : null,
                socio != null ? socio.getDireccion() : null,
                socio != null ? socio.getNumeroSocio() : null,
                socio != null ? TipoCliente.SOCIO : TipoCliente.PARTICULAR,
                socio != null ? socio.getEstado() : null,
                cliente.getNotas(),
                ultimaCuotaDto,
                cliente.getCreatedAt(),
                cliente.getUpdatedAt(),
                cliente.getCreatedBy(),
                cliente.getUpdatedBy()
        );
    }

    public static ListadoClientesResponseDto toListadoResponseDto(Cliente cliente, UltimaCuotaDto ultimaCuotaDto) {
        Socio socio = cliente instanceof Socio s ? s : null;
        return new ListadoClientesResponseDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getCedula(),
                cliente.getMail(),
                socio != null ? TipoCliente.SOCIO : TipoCliente.PARTICULAR,
                socio != null ? socio.getNumeroSocio() : null,
                socio != null ? socio.getEstado() : null,
                socio != null ? ultimaCuotaDto : null
        );
    }
    public static BusquedaCedulaResponseDto toBusquedaCedulaResponseDto(Cliente cliente) {
    Socio socio = cliente instanceof Socio s ? s : null;
    return new BusquedaCedulaResponseDto(
            cliente.getId(),
            cliente.getNombreCompleto(),
            cliente.getCedula(),
            cliente.getTelefono(),
            cliente.getMail(),
            cliente.getNotas(),
            socio != null ? TipoCliente.SOCIO : TipoCliente.PARTICULAR
    );
}

    public static EstadoSocioResponseDto toEstadoSocioResponseDto(Socio socio) {
        return new EstadoSocioResponseDto(
                socio.getId(),
                socio.getEstado(),
                socio.getNumeroSocio()
        );
    }

    public static ClienteDetalleReservaDto toClienteDetalleReservaDto(Cliente cliente) {
        return new ClienteDetalleReservaDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getCedula(),
                cliente.getTelefono(),
                cliente.getMail(),
                cliente instanceof Socio ? TipoCliente.SOCIO : TipoCliente.PARTICULAR
        );
    }
}
