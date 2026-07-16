package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.reservas.domain.enums.PlazoConfirmacion;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class ReservaCreacionRequestDto {

    @NotNull
    private TipoReserva tipoReserva;

    @NotNull
    private Procedencia procedencia;

    @NotNull
    @Positive
    private Long servicioId;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    @Min(0)
    private Integer cantidadTotal;

    @Min(0)
    private Integer cantidadMenores;

    @Min(0)
    private Integer cantidad;

    private Long clienteId;

    private Boolean crearCliente;

    private TipoCliente tipoCliente;

    private String cedula;

    private String nombre;

    private String celular;

    private String email;

    private String notas;

    private Boolean requiereDocumentacion;

    private Boolean requiereSena;

    private PlazoConfirmacion plazoConfirmacion;
}
