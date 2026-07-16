package com.cipolflo.server.clientes.dto;

import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ModificacionSocioRequestDto {

    @NotBlank(message = "La cédula es obligatoria")
    private String cedula;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    private String mail;

    private String notas;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El país es obligatorio")
    private String pais;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotNull(message = "El método de cobro es obligatorio")
    private MetodoCobro metodoCobro;

    @NotNull(message = "La categoría de socio es obligatoria")
    private CategoriaSocio categoriaSocio;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;
}
