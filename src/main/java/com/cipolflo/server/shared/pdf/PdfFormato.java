package com.cipolflo.server.shared.pdf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades de formateo de valores para el contenido de documentos PDF.
 *
 * <p>Centraliza el formato de valores comunes (texto con fallback, fechas, horas,
 * importes, booleanos) para que cada {@link ContenidoPdf} —el comprobante de reserva
 * y los futuros documentos— los reutilice sin repetir la lógica ni los
 * {@link DateTimeFormatter}. Los valores nulos se representan con {@code "-"}.</p>
 */
public final class PdfFormato {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final String VACIO = "-";

    private PdfFormato() {
    }

    /** Representa el valor como texto; {@code null} se muestra como {@code "-"}. */
    public static String texto(Object valor) {
        return valor == null ? VACIO : String.valueOf(valor);
    }

    /** Formatea la fecha como {@code dd/MM/yyyy}; {@code null} se muestra como {@code "-"}. */
    public static String fecha(LocalDate fecha) {
        return fecha == null ? VACIO : fecha.format(FORMATO_FECHA);
    }

    /** Formatea la hora como {@code HH:mm}; {@code null} se muestra como {@code "-"}. */
    public static String hora(LocalTime hora) {
        return hora == null ? VACIO : hora.format(FORMATO_HORA);
    }

    /** Formatea el importe con prefijo {@code $}; {@code null} se muestra como {@code "-"}. */
    public static String importe(BigDecimal importe) {
        return importe == null ? VACIO : "$" + importe.toPlainString();
    }

    /** Representa el booleano como {@code Sí}/{@code No}; {@code null} se muestra como {@code "-"}. */
    public static String booleano(Boolean valor) {
        if (valor == null) {
            return VACIO;
        }
        return Boolean.TRUE.equals(valor) ? "Sí" : "No";
    }
}
