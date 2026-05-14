package com.cipolflo.server.servicios.mapper;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;

public class ServicioMapper {

    private ServicioMapper() {}

    public static ListadoServiciosResponseDto toListadoResponseDto(Servicio servicio) {
        if (servicio.getHabilitado() == null) {
            throw new IllegalStateException("El servicio con id=" + servicio.getId() + " tiene 'habilitado' nulo");
        }
        EstadoServicio estado = servicio.getHabilitado()
                ? EstadoServicio.HABILITADO
                : EstadoServicio.DESHABILITADO;
        return new ListadoServiciosResponseDto(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getProcedencia(),
                servicio.getPrecioParticular(),
                servicio.getPrecioSocio(),
                servicio.getModalidadPrecio(),
                estado
        );
    }
}
