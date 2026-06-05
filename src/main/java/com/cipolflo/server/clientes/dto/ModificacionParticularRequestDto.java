package com.cipolflo.server.clientes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModificacionParticularRequestDto {

    @NotBlank(message = "La cédula es obligatoria")
    private String cedula;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    private String mail;

    private String notas;
}
