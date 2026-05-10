package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.validation.constraints.Size;

public record ListadoServiciosRequestDto(
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,
        Procedencia procedencia,
        EstadoServicio estado
) {}
