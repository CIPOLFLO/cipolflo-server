package com.cipolflo.server.servicios.exception;

public class ConfirmacionDevolucionRequeridaException extends RuntimeException{

    public ConfirmacionDevolucionRequeridaException(){
        super("Existen reservas pagas. Debe confirmar la devolución para cancelarlas");
    }
}
