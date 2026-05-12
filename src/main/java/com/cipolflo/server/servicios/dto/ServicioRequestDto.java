package com.cipolflo.server.servicios.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServicioRequestDto {
    @NotNull(message = "El campo 'habilitado' es obligatorio")
    private Boolean habilitado;
    private List<Long> reservasACancelar;
    private Boolean confirmarDevolucion;
}
