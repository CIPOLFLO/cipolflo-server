package com.cipolflo.server.servicios.validator;

import com.cipolflo.server.servicios.dto.ModificacionServicioDto;
import lombok.Getter;

@Getter
public class ModificacionValidationContext {

    private final Long id;
    private final String nombre;
    private final Integer capacidad;
    private final Integer cantidad;

    private ModificacionValidationContext(Long id, ModificacionServicioDto dto) {
        this.id = id;
        this.nombre = dto.getNombre();
        this.capacidad = dto.getCapacidad();
        this.cantidad = dto.getCantidad();
    }

    public static ModificacionValidationContext from(Long id, ModificacionServicioDto dto) {
        return new ModificacionValidationContext(id, dto);
    }
}
