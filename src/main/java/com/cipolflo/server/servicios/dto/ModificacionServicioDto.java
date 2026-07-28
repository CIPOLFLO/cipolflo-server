package com.cipolflo.server.servicios.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ModificacionServicioDto {

    @NotBlank(message = "El nombre no puede ser vacío")
    private String nombre;

    @PositiveOrZero(message = "La capacidad debe ser mayor o igual a 0")
    private Integer capacidad;

    @PositiveOrZero(message = "La cantidad debe ser mayor o igual a 0")
    private Integer cantidad;

    @PositiveOrZero(message = "El costo por persona extra debe ser mayor o igual a cero")
    private BigDecimal costoPersonaExtra;

    @Valid
    @NotEmpty(message = "Debe indicar al menos una tarifa")
    private List<TarifaServicioRequestDto> tarifas;
}
