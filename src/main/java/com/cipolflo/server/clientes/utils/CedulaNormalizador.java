package com.cipolflo.server.clientes.utils;

import java.util.regex.Pattern;

public class CedulaNormalizador {

    private CedulaNormalizador() {}

    private static final Pattern PATRON_CEDULA = Pattern.compile("^\\d+(\\.\\d+)*(-\\d+)?$");

    public static boolean esFormatoValido(String cedula) {
        if (cedula == null || cedula.isBlank()) return false;
        return PATRON_CEDULA.matcher(cedula.trim()).matches();
    }

    public static String normalizar(String cedula) {
        if (cedula == null) return "";
        return cedula.chars()
                .filter(Character::isDigit)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}