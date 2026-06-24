package com.cipolflo.server.shared.export;

public final class FormulaSanitizer {

    private FormulaSanitizer() {
    }

    public static String sanitizar(String valor) {
        if (valor == null || valor.isEmpty()) {
            return valor;
        }

        char primerCaracter = valor.charAt(0);

        if (primerCaracter == '=' ||
                primerCaracter == '+' ||
                primerCaracter == '-' ||
                primerCaracter == '@' ||
                primerCaracter == '\t' ||
                primerCaracter == '\r') {
            return "'" + valor;
        }

        return valor;
    }
}
