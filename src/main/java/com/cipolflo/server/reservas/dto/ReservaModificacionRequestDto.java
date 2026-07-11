package com.cipolflo.server.reservas.dto;
import java.time.LocalDate;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservaModificacionRequestDto {

    @NotNull
    @Positive
    private Long servicioId;

    @NotNull
    private Procedencia procedencia;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @Min(0)
    private Integer cantidadTotal;

    @Min(0)
    private Integer cantidadMenores;

    @Min(0)
    private Integer cantidad;

    private String notas;
}