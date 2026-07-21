package com.cipolflo.server.clientes.pdf;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.utils.PeriodoCuotaFormatter;
import com.cipolflo.server.shared.pdf.ContenidoPdf;
import com.cipolflo.server.shared.pdf.PdfWriter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Cuerpo específico del comprobante de pago de cuota en PDF.
 *
 * <p>Implementa {@link ContenidoPdf} recibiendo los datos ya resueltos (nombre del
 * socio, periodos cubiertos, monto total y método de cobro) y delega el armado del
 * mensaje en prosa a {@link PeriodoCuotaFormatter}, sin mezclar esa lógica con el
 * dibujo del PDF, siguiendo el mismo criterio que
 * {@code ComprobanteReservaContenidoPdf}/{@code ComprobanteAltaSocioContenidoPdf}.</p>
 */
public class ComprobantePagoCuotaContenidoPdf implements ContenidoPdf {

    private final String nombreCliente;
    private final List<YearMonth> periodos;
    private final BigDecimal montoTotal;
    private final MetodoCobro metodoCobro;

    public ComprobantePagoCuotaContenidoPdf(
            String nombreCliente,
            List<YearMonth> periodos,
            BigDecimal montoTotal,
            MetodoCobro metodoCobro
    ) {
        this.nombreCliente = nombreCliente;
        this.periodos = periodos;
        this.montoTotal = montoTotal;
        this.metodoCobro = metodoCobro;
    }

    @Override
    public void escribir(PdfWriter writer) {
        writer.escribirTitulo("Comprobante de Pago de Cuota");
        writer.escribirTitulo("Asociación Civil Círculo Policial de Flores");
        writer.linea();

        writer.escribirParrafo(
                PeriodoCuotaFormatter.armarMensajePago(nombreCliente, periodos, montoTotal, metodoCobro));
    }
}