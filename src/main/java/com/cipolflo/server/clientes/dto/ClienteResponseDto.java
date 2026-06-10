package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.shared.dto.AuditInfoDto;
import com.cipolflo.server.shared.dto.ResponseDto;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
public class ClienteResponseDto extends AuditInfoDto implements ResponseDto {

    private final Long id;
    private final String nombre;
    private final String cedula;
    private final LocalDate fechaNacimiento;
    private final String telefono;
    private final String email;
    private final MetodoCobro metodoCobro;
    private final String pais;
    private final String departamento;
    private final String ciudad;
    private final String direccion;
    private final Integer numeroSocio;
    private final TipoCliente tipoCliente;
    private final EstadoSocio estado;
    private final String observaciones;
    private final UltimaCuotaPagaDto ultimaCuotaPaga;

    public ClienteResponseDto(
            Long id, String nombre, String cedula, LocalDate fechaNacimiento,
            String telefono, String email, MetodoCobro metodoCobro,
            String pais, String departamento, String ciudad, String direccion,
            Integer numeroSocio, TipoCliente tipoCliente, EstadoSocio estado, String observaciones,
            Instant createdAt, Instant updatedAt, String createdBy, String updatedBy, UltimaCuotaPagaDto ultimaCuotaPaga) {
        super(createdAt, updatedAt, createdBy, updatedBy);
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.email = email;
        this.metodoCobro = metodoCobro;
        this.pais = pais;
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.numeroSocio = numeroSocio;
        this.tipoCliente = tipoCliente;
        this.estado = estado;
        this.observaciones = observaciones;
        this.ultimaCuotaPaga = ultimaCuotaPaga;

    }
}
