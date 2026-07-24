package com.cipolflo.server.servicios.exception;

public class TarifaServicioNotFoundException extends RuntimeException {

    public TarifaServicioNotFoundException(Long tarifaId, Long servicioId) {
        super("Tarifa no encontrada con id: " + tarifaId + " para el servicio " + servicioId);
    }
}