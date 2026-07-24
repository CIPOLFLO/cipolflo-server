package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TarifaServicioRequestDto {

    /*
     * Es nulo al registrar una tarifa nueva.
     * Tiene valor cuando se modifica una tarifa existente.
     */
    private Long id;

    @NotNull(message = "El tipo de cliente de la tarifa es obligatorio")
    private TipoClienteTarifa tipoCliente;

    @NotNull(message = "El precio de la tarifa es obligatorio")
    @DecimalMin(
            value = "0.01",
            message = "El precio de la tarifa debe ser mayor a cero"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "El precio de la tarifa admite hasta 2 decimales"
    )
    private BigDecimal precio;

    @NotNull(message = "La modalidad de precio es obligatoria")
    private ModalidadPrecio modalidadPrecio;

    private Integer antiguedadMinima;

    private Integer antiguedadMaxima;
}
