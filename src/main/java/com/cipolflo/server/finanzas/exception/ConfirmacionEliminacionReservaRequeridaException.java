package com.cipolflo.server.finanzas.exception;

public class ConfirmacionEliminacionReservaRequeridaException extends RuntimeException {

    public ConfirmacionEliminacionReservaRequeridaException() {
        super("El ingreso corresponde a una reserva ya finalizada o cancelada. "
                + "Se sugiere registrar un egreso asociado en su lugar. "
                + "Si se elimina de todas formas, no se modificará la información de la reserva.");
    }
}
