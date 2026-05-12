package com.cipolflo.server.servicios.exception;

public class CampoObligatorioException extends RuntimeException{

    public CampoObligatorioException(String campo){
        super("El campo " + campo + " es obligatorio");
    }
}
