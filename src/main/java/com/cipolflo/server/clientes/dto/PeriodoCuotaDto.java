package com.cipolflo.server.clientes.dto;

public record PeriodoCuotaDto(
        Integer anio,
        Integer mes,
        String nombreMes,
        String descripcion
) {}