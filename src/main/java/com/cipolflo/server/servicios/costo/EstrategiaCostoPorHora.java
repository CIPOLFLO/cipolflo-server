package com.cipolflo.server.servicios.costo;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EstrategiaCostoPorHora implements EstrategiaCosto {

    @Override
    public BigDecimal calcular(CalculoCostoParams params) {
        return params.servicio().precioBase(params.tipoCliente())
                .multiply(BigDecimal.valueOf(params.numeroHoras()));
    }
}
