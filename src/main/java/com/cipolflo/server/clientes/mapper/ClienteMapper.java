package com.cipolflo.server.clientes.mapper;

import java.util.List;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Empresa;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.*;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;

import static com.cipolflo.server.shared.export.ExportFormatter.orEmpty;
import static com.cipolflo.server.shared.export.ExportFormatter.orNA;

public class ClienteMapper {

    private ClienteMapper() {}

    private static TipoCliente tipoDeCliente(Cliente cliente) {
        if (cliente instanceof Socio) return TipoCliente.SOCIO;
        if (cliente instanceof Empresa) return TipoCliente.EMPRESA;
        return TipoCliente.PARTICULAR;
    }

    private static String pais(Cliente cliente) {
        if (cliente instanceof Socio s) return s.getPais();
        if (cliente instanceof Empresa e) return e.getPais();
        return null;
    }

    private static String departamento(Cliente cliente) {
        if (cliente instanceof Socio s) return s.getDepartamento();
        if (cliente instanceof Empresa e) return e.getDepartamento();
        return null;
    }

    private static String direccion(Cliente cliente) {
        if (cliente instanceof Socio s) return s.getDireccion();
        if (cliente instanceof Empresa e) return e.getDireccion();
        return null;
    }

    public static ClienteResponseDto toDetalleResponseDto(Cliente cliente, UltimaCuotaDto ultimaCuotaDto) {
        Socio socio = cliente instanceof Socio s ? s : null;
        return new ClienteResponseDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getCedula(),
                cliente instanceof Empresa e ? e.getRut() : null,
                socio != null ? socio.getFechaNacimiento() : null,
                cliente.getTelefono(),
                cliente.getMail(),
                socio != null ? socio.getMetodoCobro() : null,
                socio != null ? socio.getPais() : null,
                socio != null ? socio.getDepartamento() : null,
                socio != null ? socio.getCiudad() : null,
                socio != null ? socio.getDireccion() : null,
                socio != null ? socio.getNumeroSocio() : null,
                tipoDeCliente(cliente),
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
                cliente instanceof Empresa e ? e.getRut() : null,
                cliente.getMail(),
                tipoDeCliente(cliente),
                socio != null ? socio.getNumeroSocio() : null,
                socio != null ? socio.getEstado() : null,
                socio != null ? ultimaCuotaDto : null
        );
    }
    public static BusquedaCedulaResponseDto toBusquedaCedulaResponseDto(Cliente cliente) {
    return new BusquedaCedulaResponseDto(
            cliente.getId(),
            cliente.getNombreCompleto(),
            cliente.getCedula(),
            cliente.getTelefono(),
            cliente.getMail(),
            cliente.getNotas(),
            tipoDeCliente(cliente)
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
                tipoDeCliente(cliente)
        );
    }

    public static List<String> toExportFila(Cliente cliente){
        Socio socio = cliente instanceof Socio s ? s : null;
        return List.of(
                orEmpty(cliente.getNombreCompleto()),
                orNA(socio != null ? socio.getNumeroSocio() : null),
                orEmpty(cliente.getCedula()),
                orEmpty(cliente instanceof Empresa e ? e.getRut() : null),
                orEmpty(cliente.getMail()),
                orNA(socio != null ? socio.getEstado().toString() : null),
                orEmpty(cliente.getTelefono()),
                orEmpty(cliente.getNotas()),
                orNA(socio != null ? socio.getMetodoCobro().toString() : null),
                orEmpty(pais(cliente)),
                orEmpty(departamento(cliente)),
                orEmpty(direccion(cliente)),
                orEmpty(socio != null ? socio.getFechaIngreso() : null),
                orEmpty(socio != null ? socio.getFechaUltimoPago() : null)
        );
    }
}
