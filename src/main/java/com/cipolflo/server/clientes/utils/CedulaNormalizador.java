package com.cipolflo.server.clientes.utils;

import java.util.regex.Pattern;

public class CedulaNormalizador {

    private CedulaNormalizador() {}

  
    public static String normalizar(String cedula) {
        if (cedula == null) return "";
        return cedula.replaceAll("[.\\-]", "");
    }
}
