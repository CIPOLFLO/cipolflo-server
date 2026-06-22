package com.cipolflo.server.shared.export;

public final class FormulaSanitizer{
    private static final String PREFIJOS_PELIGROSOS ="=+-@\t\r";
    private FormulaSanitizer(){}

    public static String sanitizar(String valor){
        if(valor == null || valor.isEmpty()){
            return valor;
        }
        if(PREFIJOS_PELIGROSOS.indexOf(valor.charAt(0)) >=0){
            return "'" + valor;
        }
        return valor;
    }
}