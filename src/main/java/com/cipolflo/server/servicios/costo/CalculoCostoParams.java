package com.cipolflo.server.servicios.costo;


import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;

public record CalculoCostoParams(
        Servicio servicio,
        TarifaServicio tarifa,
        Integer cantidadTotal,
        Integer cantidad,
        Integer cantidadMenores,
        long numeroDias,
        long numeroHoras
) {}
