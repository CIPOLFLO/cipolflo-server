package com.cipolflo.server.clientes.utils;

public class CedulaNormalizador {

    private CedulaNormalizador() {}
    private static final String PATRON_CEDULA = "^\\d+(\\.\\d+)*(-\\d+)?$";

    public static boolean esFormatoValido(String cedula) {
        if (cedula == null || cedula.isBlank()) return false;
        return cedula.trim().matches(PATRON_CEDULA);
    }
    public static String normalizar(String cedula) {
         if (cedula == null) return "";
    return cedula.chars()
            .filter(Character::isDigit)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
}
