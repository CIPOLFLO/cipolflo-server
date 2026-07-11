package com.cipolflo.server.finanzas.exception;

public class EliminacionEgresoReservaNoPermitidaException extends RuntimeException {

    public EliminacionEgresoReservaNoPermitidaException() {
        super("Los egresos asociados a una reserva no pueden eliminarse. "
                + "Si se cargó por error, registrá un ingreso que lo anule.");
    }
}
