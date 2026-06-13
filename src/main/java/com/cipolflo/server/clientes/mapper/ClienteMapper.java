package com.cipolflo.server.clientes.mapper;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.PagoCuota;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.dto.UltimaCuotaPagaDto;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

public class ClienteMapper {

    private ClienteMapper() {}

    public static ClienteResponseDto toDetalleResponseDto(Cliente cliente, PagoCuota ultimaCuotaPaga, String mesCorrespondiente) {
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
                cliente.getCreatedAt(),
                cliente.getUpdatedAt(),
                cliente.getCreatedBy(),
                cliente.getUpdatedBy(),
                toUltimaCuotaPagaDto(socio, ultimaCuotaPaga, mesCorrespondiente)
        );
    }

    public static ListadoClientesResponseDto toListadoResponseDto(Cliente cliente) {
        Socio socio = cliente instanceof Socio s ? s : null;
        return new ListadoClientesResponseDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getCedula(),
                cliente.getMail(),
                socio != null ? TipoCliente.SOCIO : TipoCliente.PARTICULAR,
                socio != null ? socio.getNumeroSocio() : null,
                socio != null ? socio.getEstado() : null
        );
    }
    private static UltimaCuotaPagaDto toUltimaCuotaPagaDto(Socio socio, PagoCuota ultimaCuotaPaga, String mesCorrespondiente) {
        if (socio == null || ultimaCuotaPaga == null) {
            return null;
        }
        return new UltimaCuotaPagaDto(
                mesCorrespondiente,
                ultimaCuotaPaga.getFecha(),
                ultimaCuotaPaga.getImporte()
        );
    }
}
