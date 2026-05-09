package com.cipolflo.server.servicios.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServicioRequestDto {
    private Boolean habilitado;
    private Boolean cancelarReservas;
    private List<Long> reservasACancelar;
    private Boolean confirmarDevolucion;
}
