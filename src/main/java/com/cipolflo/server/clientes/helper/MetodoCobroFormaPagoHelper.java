package com.cipolflo.server.clientes.helper;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.shared.enums.FormaPago;


public final class MetodoCobroFormaPagoHelper {

    private MetodoCobroFormaPagoHelper() {
    }

    public static FormaPago toFormaPago(MetodoCobro metodoCobro) {
        return switch (metodoCobro) {
            case EFECTIVO, EN_SEDE -> FormaPago.EFECTIVO;
            case TRANSFERENCIA -> FormaPago.TRANSFERENCIA;
            case DEBITO -> FormaPago.DEBITO;
            case DESCUENTO_SALARIAL -> FormaPago.TRANSFERENCIA;
        };
    }
}