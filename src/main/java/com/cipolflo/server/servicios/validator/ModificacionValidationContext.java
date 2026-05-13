package com.cipolflo.server.servicios.validator;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ModificacionServicioDto;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ModificacionValidationContext {

    private final Long id;
    private final String nombre;
    private final BigDecimal precioParticular;
    private final BigDecimal precioSocio;
    private final ModalidadPrecio modalidadPrecio;
    private final Integer capacidad;
    private final Integer cantidad;

    private ModificacionValidationContext(Long id, ModificacionServicioDto dto) {
        this.id = id;
        this.nombre = dto.getNombre();
        this.precioParticular = dto.getPrecioParticular();
        this.precioSocio = dto.getPrecioSocio();
        this.modalidadPrecio = dto.getModalidadPrecio();
        this.capacidad = dto.getCapacidad();
        this.cantidad = dto.getCantidad();
    }

    public static ModificacionValidationContext from(Long id, ModificacionServicioDto dto) {
        return new ModificacionValidationContext(id, dto);
    }
}
