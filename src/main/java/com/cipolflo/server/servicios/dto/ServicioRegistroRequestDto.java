package com.cipolflo.server.servicios.dto;

import java.math.BigDecimal;
import java.util.List;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServicioRegistroRequestDto {

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String nombre;

    @NotNull(message = "La procedencia es obligatoria")
    private Procedencia procedencia;

    @NotNull(message = "El precio socio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio socio debe ser mayor que cero")
    private BigDecimal precioSocio;

    @NotNull(message = "El precio particular es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio particular debe ser mayor que cero")
    private BigDecimal precioParticular;

    @NotNull(message = "La modalidad de precio es obligatoria")
    private ModalidadPrecio modalidadPrecio;

    @Positive(message = "La capacidad debe ser un número positivo")
    private Integer capacidad;

    @Positive(message = "La cantidad debe ser un número positivo")
    private Integer cantidad;

    @DecimalMin(value = "0.0", inclusive = true, message = "El costo por persona extra debe ser mayor o igual a cero")
    private BigDecimal costoPersonaExtra;

    @Valid
    @NotEmpty(message = "Debe indicar al menos una tarifa")
    private List<TarifaServicioRequestDto> tarifas;
}
