package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.shared.dto.ResponseDto;
import lombok.Getter;

@Getter
public class ListadoClientesResponseDto implements ResponseDto {

    private final Long id;
    private final String nombreCompleto;
    private final String cedula;
    private final String email;
    private final TipoCliente tipoCliente;
    private final Integer numeroSocio;
    private final EstadoSocio estado;
    private final UltimaCuotaDto ultimaCuotaDto;

    public ListadoClientesResponseDto(Long id, String nombreCompleto, String cedula, String email, TipoCliente tipoCliente, Integer numeroSocio, EstadoSocio estado, UltimaCuotaDto ultimaCuotaDto) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.cedula = cedula;
        this.email = email;
        this.tipoCliente = tipoCliente;
        this.numeroSocio = numeroSocio;
        this.estado = estado;
        this.ultimaCuotaDto = ultimaCuotaDto;
    }
}
