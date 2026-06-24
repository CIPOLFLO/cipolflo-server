package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.enums.Procedencia;

public record ServicioDetalleReservaDto(
        Long id,
        String nombre,
        Procedencia procedencia,
        ModalidadPrecio modalidadPrecio
) {}
