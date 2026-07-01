package com.cipolflo.server.shared.export;

import java.math.BigDecimal;

public final class ExportFormatter {

    private ExportFormatter() {
    }

    public static String orEmpty(Object value) {
        return value != null ? value.toString() : "";
    }

    public static String orEmpty(BigDecimal value) {
        return value != null ? value.toPlainString() : "";
    }

    public static String orEmpty(String value, String fallback) {
        return value != null ? value : orEmpty(fallback);
    }

    public static String orNA(Object value) {
        return value != null ? value.toString() : "N/A";
    }

    public static String formatearBooleano(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Sí" : "No";
    }
}
