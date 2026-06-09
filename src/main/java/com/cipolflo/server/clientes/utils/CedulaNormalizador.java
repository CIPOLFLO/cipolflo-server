package com.cipolflo.server.clientes.utils;

import java.util.regex.Pattern;

public class CedulaNormalizador {

    private CedulaNormalizador() {}

   public static boolean esFormatoValido(String cedula) {
        if (cedula == null || cedula.isBlank()) return false;
        String trimmed = cedula.trim();
        return trimmed.chars()
                .allMatch(c -> Character.isDigit(c) || c == '.' || c == '-')
                && trimmed.chars().filter(c -> c == '-').count() <= 1
                && !trimmed.startsWith(".")
                && !trimmed.startsWith("-")
                && !trimmed.endsWith(".")
                && !trimmed.endsWith("-");
    }

    public static String normalizar(String cedula) {
        if (cedula == null) return "";
        return cedula.replaceAll("[.\\-]", "");
    }
}
