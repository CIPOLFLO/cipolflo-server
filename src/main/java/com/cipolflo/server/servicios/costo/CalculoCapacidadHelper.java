package com.cipolflo.server.servicios.costo;

import java.math.BigDecimal;

class CalculoCapacidadHelper {

    private CalculoCapacidadHelper() {}

    static BigDecimal costoBase(CalculoCostoParams params) {
        int cantidadTotal = params.cantidadTotal() != null ? params.cantidadTotal() : 0;
        int menores = params.cantidadMenores() != null ? params.cantidadMenores() : 0;
        int capacidad = params.servicio().getCapacidad() != null ? params.servicio().getCapacidad() : 0;
        BigDecimal costoExtra = params.servicio().getCostoPersonaExtra() != null
                ? params.servicio().getCostoPersonaExtra()
                : BigDecimal.ZERO;

        int adultosEquivalentes = cantidadTotal - menores;
        int excedente = Math.max(0, adultosEquivalentes - capacidad);

        return params.servicio().precioBase(params.tipoCliente())
                .add(costoExtra.multiply(BigDecimal.valueOf(excedente)));
    }
}
