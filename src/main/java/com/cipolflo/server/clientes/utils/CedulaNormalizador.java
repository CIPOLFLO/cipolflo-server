package com.cipolflo.server.clientes.utils;

public class CedulaNormalizador {

    private CedulaNormalizador() {}

    public static String normalizar(String cedula) {
        if (cedula == null) return "";
        return cedula.replaceAll("\\D", "");
    }
}
