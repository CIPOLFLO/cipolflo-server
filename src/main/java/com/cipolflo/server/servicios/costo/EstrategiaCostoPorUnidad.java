package com.cipolflo.server.servicios.costo;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EstrategiaCostoPorUnidad implements EstrategiaCosto {

    @Override
    public BigDecimal calcular(CalculoCostoParams params) {
        int cantidad = params.cantidad() != null ? params.cantidad() : 1;
        return params.servicio().precioBase(params.tipoCliente())
                .multiply(BigDecimal.valueOf(cantidad));
    }
}
