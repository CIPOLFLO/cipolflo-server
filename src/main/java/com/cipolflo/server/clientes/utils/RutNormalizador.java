package com.cipolflo.server.clientes.utils;

public class RutNormalizador {

    private RutNormalizador() {}

    public static String normalizar(String rut) {
        if (rut == null) return "";
        return rut.replaceAll("\\D", "");
    }
}
