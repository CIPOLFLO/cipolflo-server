package com.cipolflo.server.servicios.dto;

import java.math.BigDecimal;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.enums.Procedencia;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ServicioRegistroRequestDto {

@NotBlank(message = "El nombre del servicio es obligatorio")
    private String nombre;
    


@NotNull(message="La procedencia es obligatoria")
    private Procedencia procedencia;


@NotNull(message = "El precio es obligatorio")
@DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    private BigDecimal precioSocio;

@NotNull(message = "El precio es obligatorio")
@DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    private BigDecimal precioParticular;

@NotNull(message = "La modalidad de precio es obligatorio")
private ModalidadPrecio modalidadPrecio;

@Positive(message = "El orden debe ser un número positivo")
    private Integer capacidad;


@Positive(message = "El orden debe ser un número positivo")
    private Integer cantidad;    



    public ServicioRegistroRequestDto(String nombre, Procedencia procedencia, BigDecimal precioSocio, BigDecimal precioParticular, ModalidadPrecio modalidadPrecio, Integer capacidad, Integer cantidad) {
        this.nombre = nombre;
        this.procedencia = procedencia;
        this.precioSocio = precioSocio;
        this.precioParticular = precioParticular;
        this.modalidadPrecio = modalidadPrecio;
        this.capacidad = capacidad;
        this.cantidad = cantidad;

    }
}