package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class CalculoCostoRequestDto {

    @NotNull(message = "El id del servicio es obligatorio")
    @Positive(message = "El id del servicio debe ser un número positivo")
    private Long servicioId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    @Min(value = 0, message = "La cantidad total no puede ser negativa")
    private Integer cantidadTotal;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    @Min(value = 0, message = "La cantidad de menores no puede ser negativa")
    private Integer cantidadMenores;

    private TipoCliente tipoCliente;
}
