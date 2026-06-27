package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.servicios.domain.Servicio;

public record CalculoCostoParams(
        Servicio servicio,
        TipoCliente tipoCliente,
        Integer cantidadTotal,
        Integer cantidad,
        Integer cantidadMenores,
        long numeroDias,
        long numeroHoras
) {}
