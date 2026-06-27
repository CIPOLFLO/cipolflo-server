package com.cipolflo.server.servicios.costo;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EstrategiaCostoPorDiaPorPersona implements EstrategiaCosto {

    @Override
    public BigDecimal calcular(CalculoCostoParams params) {
        return CalculoCapacidadHelper.costoBase(params)
                .multiply(BigDecimal.valueOf(params.numeroDias()));
    }
}
