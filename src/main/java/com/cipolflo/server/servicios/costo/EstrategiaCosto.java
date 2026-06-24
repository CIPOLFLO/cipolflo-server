package com.cipolflo.server.servicios.costo;

import java.math.BigDecimal;

public interface EstrategiaCosto {
    BigDecimal calcular(CalculoCostoParams params);
}
