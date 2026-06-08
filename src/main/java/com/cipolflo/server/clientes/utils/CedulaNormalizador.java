package com.cipolflo.server.clientes.utils;

import java.util.regex.Pattern;

public class CedulaNormalizador {

    private CedulaNormalizador() {}

    private static final Pattern SOLO_DIGITOS = Pattern.compile("^\\d+$");
    private static final Pattern FORMATO_PUNTOS = Pattern.compile("^\\d{1,3}(\\.\\d{3})*-\\d$");

    public static boolean esFormatoValido(String cedula) {
        if (cedula == null || cedula.isBlank()) return false;
        String trimmed = cedula.trim();
        return SOLO_DIGITOS.matcher(trimmed).matches()
                || FORMATO_PUNTOS.matcher(trimmed).matches();
    }

    public static String normalizar(String cedula) {
        if (cedula == null) return "";
        return cedula.chars()
                .filter(Character::isDigit)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}