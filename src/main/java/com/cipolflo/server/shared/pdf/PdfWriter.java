package com.cipolflo.server.shared.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper que envuelve {@link PDPageContentStream} y centraliza el manejo de
 * coordenadas, fuentes, márgenes y salto de página automático, exponiendo
 * métodos de alto nivel reutilizables entre documentos.
 *
 * <p>Es la única capa que conoce la mecánica de PDFBox; cada {@link ContenidoPdf}
 * dibuja llamando a estos métodos y nunca toca {@code PDPageContentStream}
 * directamente. Los métodos públicos no propagan {@link IOException}: la envuelven
 * en {@link UncheckedIOException} para que {@link ContenidoPdf#escribir} quede
 * limpio de {@code throws}.</p>
 */
public class PdfWriter {

    private static final float MARGEN = 50f;
    private static final float TAMANO_TITULO = 16f;
    private static final float TAMANO_SUBTITULO = 13f;
    private static final float TAMANO_TEXTO = 11f;
    private static final float TAMANO_FECHA = 9f;
    private static final float INTERLINEADO = 16f;
    private static final float SANGRIA = 12f;

    private static final PDType1Font FUENTE_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FUENTE_NEGRITA =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final DateTimeFormatter FORMATO_FECHA_GENERACION =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PDDocument document;
    private final float anchoContenido;
    private final float tope;

    private PDPage paginaActual;
    private PDPageContentStream stream;
    private float y;

    public PdfWriter(PDDocument document) {
        this.document = document;
        PDRectangle tamano = PDRectangle.A4;
        this.anchoContenido = tamano.getWidth() - 2 * MARGEN;
        this.tope = tamano.getHeight() - MARGEN;
        nuevaPagina();
    }

    /**
     * Dibuja la fecha/hora actual (formato {@code dd/MM/yyyy HH:mm}, zona por defecto
     * del sistema) como encabezado común. Pensado para invocarse desde la base, no
     * desde cada documento.
     */
    public void escribirFechaGeneracion() {
        String fechaHora = LocalDateTime
                .now(ZoneId.systemDefault())
                .format(FORMATO_FECHA_GENERACION);
        dibujarLinea(fechaHora, FUENTE_REGULAR, TAMANO_FECHA, MARGEN);
        y -= INTERLINEADO;
        espacio(INTERLINEADO / 2);
    }

    /** Escribe un título centrado en negrita, con espacio antes y después. */
    public void escribirTitulo(String texto) {
        String limpio = sanitizar(texto, FUENTE_NEGRITA);
        asegurarEspacio(INTERLINEADO * 1.5f);
        float ancho = anchoTexto(limpio, FUENTE_NEGRITA, TAMANO_TITULO);
        float x = MARGEN + Math.max(0, (anchoContenido - ancho) / 2);
        dibujarLinea(limpio, FUENTE_NEGRITA, TAMANO_TITULO, x);
        y -= INTERLINEADO * 1.5f;
    }

    /** Escribe un subtítulo de sección en negrita, alineado al margen izquierdo. */
    public void escribirSubtitulo(String texto) {
        String limpio = sanitizar(texto, FUENTE_NEGRITA);
        asegurarEspacio(INTERLINEADO);
        dibujarLinea(limpio, FUENTE_NEGRITA, TAMANO_SUBTITULO, MARGEN);
        y -= INTERLINEADO * 1.5f;
    }

    /** Escribe un campo {@code etiqueta: valor} (etiqueta en negrita, valor normal), con sangría izquierda. */
    public void escribirCampo(String etiqueta, String valor) {
        String label = sanitizar(etiqueta + ": ", FUENTE_NEGRITA);
        String value = sanitizar(valor == null || valor.isBlank() ? "-" : valor, FUENTE_REGULAR);

        float inicio = MARGEN + SANGRIA;
        float anchoLabel = anchoTexto(label, FUENTE_NEGRITA, TAMANO_TEXTO);
        List<String> lineas = ajustar(value, FUENTE_REGULAR, TAMANO_TEXTO,
                anchoContenido - SANGRIA - anchoLabel, anchoContenido - SANGRIA);

        asegurarEspacio(INTERLINEADO);
        dibujarLinea(label, FUENTE_NEGRITA, TAMANO_TEXTO, inicio);
        dibujarLinea(lineas.getFirst(), FUENTE_REGULAR, TAMANO_TEXTO, inicio + anchoLabel);
        y -= INTERLINEADO;

        for (int i = 1; i < lineas.size(); i++) {
            asegurarEspacio(INTERLINEADO);
            dibujarLinea(lineas.get(i), FUENTE_REGULAR, TAMANO_TEXTO, inicio);
            y -= INTERLINEADO;
        }
    }

    /** Escribe un párrafo con ajuste de línea, con la misma sangría izquierda que los campos. */
    public void escribirParrafo(String texto) {
        String limpio = sanitizar(texto == null ? "" : texto, FUENTE_REGULAR);
        float inicio = MARGEN + SANGRIA;
        for (String linea : ajustar(limpio, FUENTE_REGULAR, TAMANO_TEXTO,
                anchoContenido - SANGRIA, anchoContenido - SANGRIA)) {
            asegurarEspacio(INTERLINEADO);
            dibujarLinea(linea, FUENTE_REGULAR, TAMANO_TEXTO, inicio);
            y -= INTERLINEADO;
        }
    }

    /** Avanza el cursor vertical dejando un espacio en blanco. */
    public void espacio(float alto) {
        asegurarEspacio(alto);
        y -= alto;
    }

    /** Dibuja una línea horizontal separadora a lo ancho del contenido. */
    public void linea() {
        asegurarEspacio(INTERLINEADO);
        try {
            stream.moveTo(MARGEN, y);
            stream.lineTo(MARGEN + anchoContenido, y);
            stream.stroke();
        } catch (IOException e) {
            throw new UncheckedIOException("Error al dibujar línea separadora", e);
        }
        y -= INTERLINEADO;
    }

    /** Cierra el content stream actual. Debe invocarse antes de guardar el documento. */
    public void cerrar() {
        try {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error al cerrar el content stream", e);
        }
    }

    private void nuevaPagina() {
        cerrar();
        paginaActual = new PDPage(PDRectangle.A4);
        document.addPage(paginaActual);
        try {
            stream = new PDPageContentStream(document, paginaActual);
        } catch (IOException e) {
            throw new UncheckedIOException("Error al abrir el content stream", e);
        }
        y = tope;
    }

    private void asegurarEspacio(float necesario) {
        if (y - necesario < MARGEN) {
            nuevaPagina();
        }
    }

    private void dibujarLinea(String texto, PDType1Font fuente, float tamano, float x) {
        try {
            stream.beginText();
            stream.setFont(fuente, tamano);
            stream.newLineAtOffset(x, y);
            stream.showText(texto);
            stream.endText();
        } catch (IOException e) {
            throw new UncheckedIOException("Error al escribir texto en el PDF", e);
        }
    }

    private float anchoTexto(String texto, PDType1Font fuente, float tamano) {
        try {
            return fuente.getStringWidth(texto) / 1000 * tamano;
        } catch (IOException e) {
            throw new UncheckedIOException("Error al medir el ancho del texto", e);
        }
    }

    /**
     * Ajusta el texto en líneas: la primera puede usar un ancho distinto (útil para
     * que el valor de un campo empiece después de la etiqueta) y el resto usa el ancho
     * completo.
     */
    private List<String> ajustar(String texto, PDType1Font fuente, float tamano,
                                 float anchoPrimera, float anchoResto) {
        List<String> lineas = new ArrayList<>();
        float anchoMax = anchoPrimera;
        StringBuilder actual = new StringBuilder();

        for (String palabra : texto.split(" ")) {
            String candidato = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (actual.length() > 0 && anchoTexto(candidato, fuente, tamano) > anchoMax) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
                anchoMax = anchoResto;
            } else {
                actual = new StringBuilder(candidato);
            }
        }
        lineas.add(actual.toString());
        return lineas;
    }

    /**
     * Reemplaza por {@code ?} los caracteres que la fuente Standard 14 no puede
     * codificar, evitando que {@code showText} falle con caracteres fuera de WinAnsi.
     */
    private String sanitizar(String texto, PDType1Font fuente) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            String s = String.valueOf(c);
            try {
                fuente.getStringWidth(s);
                sb.append(c);
            } catch (IOException | IllegalArgumentException e) {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
