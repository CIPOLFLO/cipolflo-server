package com.cipolflo.server.servicios.exception;

public class ConfirmacionDevolucionRequeridaException extends RuntimeException{

    public ConfirmacionDevolucionRequeridaException(){
        super("Existe reservas pagas. Debe confirmar la devolución para cancelarlas");
    }
}
