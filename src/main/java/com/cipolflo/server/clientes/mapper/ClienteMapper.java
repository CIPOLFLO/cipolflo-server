package com.cipolflo.server.clientes.mapper;

import java.time.LocalDate;
import java.util.List;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.ClienteConUbicacion;
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
        return cliente instanceof ClienteConUbicacion u ? u.getPais() : null;
    }

    private static String departamento(Cliente cliente) {
        return cliente instanceof ClienteConUbicacion u ? u.getDepartamento() : null;
    }

    private static String ciudad(Cliente cliente) {
        return cliente instanceof ClienteConUbicacion u ? u.getCiudad() : null;
    }

    private static String direccion(Cliente cliente) {
        return cliente instanceof ClienteConUbicacion u ? u.getDireccion() : null;
    }

    public static ClienteResponseDto toDetalleResponseDto(Cliente cliente, UltimaCuotaDto ultimaCuotaDto, LocalDate fechaReferencia) {
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
                pais(cliente),
                departamento(cliente),
                ciudad(cliente),
                direccion(cliente),
                socio != null ? socio.getNumeroSocio() : null,
                tipoDeCliente(cliente),
                socio != null ? socio.getEstado() : null,
                socio != null ? socio.getCategoriaSocio() : null,
                socio != null ? socio.getFechaIngreso() : null,
                socio != null
                        ? socio.calcularAntiguedadEnAnios(fechaReferencia)
                        : null,
                ultimaCuotaDto,
                cliente.getNotas(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt(),
                cliente.getCreatedBy(),
                cliente.getUpdatedBy()
        );
    }

    public static ListadoClientesResponseDto toListadoResponseDto(Cliente cliente, UltimaCuotaDto ultimaCuotaDto, LocalDate fechaReferencia) {
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
                socio != null ? socio.getCategoriaSocio() : null,
                socio != null
                        ? socio.calcularAntiguedadEnAnios(fechaReferencia)
                        : null,
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

    public static EstadoSocioResponseDto toEstadoSocioResponseDto(Socio socio, Integer mesesSinPagar) {
        return new EstadoSocioResponseDto(
                socio.getId(),
                socio.getEstado(),
                socio.getNumeroSocio(),
                mesesSinPagar
        );
    }

    public static ClienteDetalleReservaDto toClienteDetalleReservaDto(Cliente cliente) {
        return new ClienteDetalleReservaDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getCedula(),
                cliente instanceof Empresa e ? e.getRut() : null,
                cliente.getTelefono(),
                cliente.getMail(),
                tipoDeCliente(cliente)
        );
    }

    public static List<String> toExportFila(Cliente cliente, LocalDate fechaReferencia){
        Socio socio = cliente instanceof Socio s ? s : null;
        return List.of(
                orEmpty(cliente.getNombreCompleto()),
                orNA(socio != null ? socio.getNumeroSocio() : null),
                orEmpty(cliente.getCedula()),
                orEmpty(cliente instanceof Empresa e ? e.getRut() : null),
                orEmpty(cliente.getMail()),
                orNA(socio != null ? socio.getEstado().toString() : null),
                orNA(socio != null ? socio.getCategoriaSocio().toString() : null),
                orEmpty(cliente.getTelefono()),
                orEmpty(cliente.getNotas()),
                orNA(socio != null ? socio.getMetodoCobro().toString() : null),
                orEmpty(pais(cliente)),
                orEmpty(departamento(cliente)),
                orEmpty(direccion(cliente)),
                orEmpty(socio != null ? socio.getFechaIngreso() : null),
                orNA( socio != null ? socio.calcularAntiguedadEnAnios(fechaReferencia) : null),
                orEmpty(socio != null ? socio.getFechaUltimoPago() : null)
        );
    }

    public static BusquedaRutResponseDto toBusquedaRutResponseDto(Empresa empresa) {
        return new BusquedaRutResponseDto(
                empresa.getId(),
                empresa.getNombreCompleto(),
                empresa.getRut(),
                empresa.getTelefono(),
                empresa.getMail(),
                empresa.getNotas(),
                TipoCliente.EMPRESA
        );
    }
}
