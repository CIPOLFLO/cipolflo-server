package com.cipolflo.server.finanzas.exception;

public class EliminacionPagoCuotaNoPermitidaException extends RuntimeException {

    public EliminacionPagoCuotaNoPermitidaException() {
        super("Los pagos de cuota no pueden eliminarse. La anulación de cuotas es una funcionalidad pendiente.");
    }
}
