package com.cipolflo.server.manuales.exception;

/** La clave solicitada no corresponde a ningún manual del catálogo. */
public class ManualNoEncontradoException extends RuntimeException {

    public ManualNoEncontradoException(String clave) {
        super("Manual no encontrado con clave: " + clave);
    }
}
