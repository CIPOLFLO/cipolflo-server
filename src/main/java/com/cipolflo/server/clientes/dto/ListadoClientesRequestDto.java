package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import jakarta.validation.constraints.Size;

public record ListadoClientesRequestDto(
        TipoCliente tipoCliente,
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,
        String identificador,
        EstadoSocio estado
) {}
