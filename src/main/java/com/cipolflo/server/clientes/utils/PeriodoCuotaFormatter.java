package com.cipolflo.server.clientes.utils;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.shared.pdf.PdfFormato;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilidad de formateo de periodos de cuota (mes/año) para el comprobante de pago.
 *
 * <p>Agrupa una lista de {@link YearMonth} por año y arma el texto en prosa usado
 * en {@code ComprobantePagoCuotaContenidoPdf}, sin depender de PDFBox ni de Spring,
 * para poder probarse de forma aislada.</p>
 */
public final class PeriodoCuotaFormatter {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-UY");

    private PeriodoCuotaFormatter() {
    }

    /**
     * Formatea los periodos agrupados por año, ordenados cronológicamente.
     *
     * <p>Dentro de un mismo año los meses se separan por coma y el último se une con
     * "y", con el sufijo {@code " de {año}"} una sola vez al final del grupo. Los
     * grupos de distintos años se separan entre sí por coma.</p>
     */
    public static String formatearMeses(List<YearMonth> periodos) {
        List<YearMonth> ordenados = periodos.stream()
                .sorted()
                .toList();

        Map<Integer, List<YearMonth>> porAnio = ordenados.stream()
                .collect(Collectors.groupingBy(YearMonth::getYear, LinkedHashMap::new, Collectors.toList()));

        List<String> grupos = new ArrayList<>();
        for (Map.Entry<Integer, List<YearMonth>> entry : porAnio.entrySet()) {
            grupos.add(formatearGrupoDeAnio(entry.getValue(), entry.getKey()));
        }

        return String.join(", ", grupos);
    }

    private static String formatearGrupoDeAnio(List<YearMonth> mesesDelAnio, int anio) {
        List<String> nombresMeses = mesesDelAnio.stream()
                .map(PeriodoCuotaFormatter::nombreMes)
                .toList();

        String mesesFormateados = unirConY(nombresMeses);

        return mesesFormateados + " de " + anio;
    }

    private static String unirConY(List<String> nombres) {
        if (nombres.size() == 1) {
            return nombres.get(0);
        }
        String inicio = String.join(", ", nombres.subList(0, nombres.size() - 1));
        String ultimo = nombres.get(nombres.size() - 1);
        return inicio + " y " + ultimo;
    }

    /**
     * Nombre del mes en español, capitalizado (ej. {@code "Noviembre"}).
     *
     * <p>Único punto de formateo de nombres de mes de la aplicación: {@code PagoCuotaService}
     * delega aquí en vez de duplicar la lógica de capitalización/locale.</p>
     */
    public static String nombreMes(YearMonth yearMonth) {
        String nombre = yearMonth.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES);
        return nombre.substring(0, 1).toUpperCase(LOCALE_ES) + nombre.substring(1);
    }

    /**
     * Arma el mensaje completo del comprobante de pago de cuota, resolviendo
     * singular/plural según la cantidad de periodos.
     */
    public static String armarMensajePago(
            String nombreCliente,
            List<YearMonth> periodos,
            BigDecimal montoTotal,
            MetodoCobro metodoCobro
    ) {
        String meses = formatearMeses(periodos);
        String importe = PdfFormato.importe(montoTotal);

        String cuerpo = periodos.size() == 1
                ? "Se registra que hoy pagó la cuota perteneciente al mes de " + meses
                : "Se registra que hoy pagó las cuotas pertenecientes a los meses de " + meses;

        return "Estimado/a " + nombreCliente + ": " + cuerpo
                + ", por el monto total de " + importe + ". "
                + "El pago fue realizado mediante " + metodoCobro + ".";
    }
}