package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
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
    private final String rut;
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
    private final CategoriaSocio categoriaSocio;
    private final LocalDate fechaIngreso;
    private final String observaciones;
    private final UltimaCuotaDto ultimaCuotaDto;

    public ClienteResponseDto(
            Long id,
            String nombre,
            String cedula,
            String rut,
            LocalDate fechaNacimiento,
            String telefono,
            String email,
            MetodoCobro metodoCobro,
            String pais,
            String departamento,
            String ciudad,
            String direccion,
            Integer numeroSocio,
            TipoCliente tipoCliente,
            EstadoSocio estado,
            CategoriaSocio categoriaSocio,
            LocalDate fechaIngreso,
            String observaciones,
            UltimaCuotaDto ultimaCuotaDto,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
        super(createdAt, updatedAt, createdBy, updatedBy);
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.rut = rut;
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
        this.categoriaSocio = categoriaSocio;
        this.fechaIngreso = fechaIngreso;
        this.observaciones = observaciones;
        this.ultimaCuotaDto = ultimaCuotaDto;
    }
}
