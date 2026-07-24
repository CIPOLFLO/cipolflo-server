package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "El precio particular es requerido")
    @Positive(message = "El precio particular debe ser mayor a 0")
    private BigDecimal precioParticular;

    @NotNull(message = "El precio socio es requerido")
    @Positive(message = "El precio socio debe ser mayor a 0")
    private BigDecimal precioSocio;

    @NotNull(message = "La modalidad de precio es requerida")
    private ModalidadPrecio modalidadPrecio;

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
