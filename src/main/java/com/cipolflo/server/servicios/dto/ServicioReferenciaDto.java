package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.shared.enums.Procedencia;

/** Referencia liviana a un servicio, para identificarlo antes de consultar algo más sobre él. */
public record ServicioReferenciaDto(Long id, String nombre, Procedencia procedencia, Boolean habilitado) {
}
