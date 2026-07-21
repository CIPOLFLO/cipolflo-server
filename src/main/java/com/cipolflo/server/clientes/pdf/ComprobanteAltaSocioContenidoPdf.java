package com.cipolflo.server.clientes.pdf;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.shared.pdf.ContenidoPdf;
import com.cipolflo.server.shared.pdf.PdfWriter;

/**
 * Cuerpo específico del comprobante de alta de socio en PDF.
 *
 * <p>Implementa {@link ContenidoPdf} recibiendo el {@link Socio} ya persistido y
 * define el contenido del comprobante llamando a los métodos de alto nivel del
 * {@link PdfWriter}, siguiendo el mismo criterio que
 * {@code ComprobanteReservaContenidoPdf}. No maneja {@code PDDocument}/páginas/streams
 * ni escribe la fecha de generación: eso lo aporta la base compartida {@code shared.pdf}.</p>
 */
public class ComprobanteAltaSocioContenidoPdf implements ContenidoPdf {

    private final Socio socio;

    public ComprobanteAltaSocioContenidoPdf(Socio socio) {
        this.socio = socio;
    }

    @Override
    public void escribir(PdfWriter writer) {
        writer.escribirTitulo("Comprobante de Alta de Socio");
        writer.escribirTitulo("Asociación Civil Círculo Policial de Flores");
        writer.linea();

        writer.escribirParrafo(armarMensaje());
    }

    private String armarMensaje() {
        return "Estimado/a " + socio.getNombreCompleto() + ": El día de hoy ha quedado registrado "
                + "como Socio de la Asociación Civil Círculo Policial de Flores. "
                + "Su número de socio asignado es el n° " + socio.getNumeroSocio() + ".";
    }
}