package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.dto.ResponseDto;
import com.cipolflo.server.shared.enums.Procedencia;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ListadoServiciosResponseDto implements ResponseDto {

    private final Long id;
    private final String nombre;
    private final Procedencia procedencia;
    private final BigDecimal precioParticular;
    private final BigDecimal precioSocio;
    private final ModalidadPrecio modalidadPrecio;
    private final EstadoServicio estado;
    private final Integer capacidad;
    private final Integer cantidad;

    public ListadoServiciosResponseDto(
            Long id,
            String nombre,
            Procedencia procedencia,
            BigDecimal precioParticular,
            BigDecimal precioSocio,
            ModalidadPrecio modalidadPrecio,
            EstadoServicio estado,
            Integer capacidad,
            Integer cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.procedencia = procedencia;
        this.precioParticular = precioParticular;
        this.precioSocio = precioSocio;
        this.modalidadPrecio = modalidadPrecio;
        this.estado = estado;
        this.capacidad = capacidad;
        this.cantidad = cantidad;
    }
}
