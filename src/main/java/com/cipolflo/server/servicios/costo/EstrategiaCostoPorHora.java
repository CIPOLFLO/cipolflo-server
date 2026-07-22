package com.cipolflo.server.servicios.costo;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EstrategiaCostoPorHora implements EstrategiaCosto {

    @Override
    public BigDecimal calcular(CalculoCostoParams params) {
        return params.tarifa().getPrecio()
                .multiply(BigDecimal.valueOf(params.numeroHoras()));
    }
}
