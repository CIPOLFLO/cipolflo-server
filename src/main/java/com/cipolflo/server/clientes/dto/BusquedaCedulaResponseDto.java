package com.cipolflo.server.clientes.dto;
import lombok.Getter;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.shared.dto.ResponseDto;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
@Getter
public class BusquedaCedulaResponseDto implements ResponseDto {

    private final Long id;
    private final String nombre;
    private final String cedula;
    private final String telefono;
    private final String mail;
    private final String observaciones;
    private final TipoCliente tipoCliente;

    public BusquedaCedulaResponseDto(
            Long id, String nombre, String cedula,
            String telefono, String mail, String observaciones,
            TipoCliente tipoCliente) {
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.telefono = telefono;
        this.mail = mail;
        this.observaciones = observaciones;
        this.tipoCliente = tipoCliente;
    
    }
}