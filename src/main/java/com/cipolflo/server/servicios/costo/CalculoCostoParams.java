package com.cipolflo.server.servicios.costo;


import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;

import java.math.BigDecimal;

public record CalculoCostoParams(
        Servicio servicio,
        BigDecimal precioUnitario,
        Integer cantidadTotal,
        Integer cantidad,
        Integer cantidadMenores,
        long numeroDias,
        long numeroHoras
) {
}
