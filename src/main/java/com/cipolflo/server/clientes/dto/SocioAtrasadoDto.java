package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;

public record SocioAtrasadoDto(Long id, String nombre, String cedula, Integer mesesSinPagar, EstadoSocio estado) {
}
