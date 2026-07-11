package com.cipolflo.server.clientes.dto;
import lombok.Getter;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.shared.dto.ResponseDto;


@Getter
public class BusquedaRutResponseDto implements ResponseDto {

    private final Long id;
    private final String nombre;
    private final String rut;
    private final String telefono;
    private final String mail;
    private final String observaciones;
    private final TipoCliente tipoCliente;

    public BusquedaRutResponseDto(
            Long id, String nombre, String rut,
            String telefono, String mail, String observaciones,
            TipoCliente tipoCliente) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.telefono = telefono;
        this.mail = mail;
        this.observaciones = observaciones;
        this.tipoCliente = tipoCliente;
    }
}
