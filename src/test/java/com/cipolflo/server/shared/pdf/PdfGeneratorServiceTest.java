package com.cipolflo.server.shared.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfGeneratorServiceTest {

    private final PdfGeneratorService service = new PdfGeneratorService();

    @Test
    void deberiaGenerarPdfValidoNoVacio() throws IOException {
        ContenidoPdf contenido = writer -> {
            writer.escribirTitulo("Documento de prueba");
            writer.escribirCampo("Campo", "Valor");
        };

        byte[] pdf = service.generar(contenido);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "El PDF no debería estar vacío");
        assertTrue(esPdfValido(pdf), "El resultado debería ser un PDF válido");
    }

    @Test
    void deberiaPaginarCuandoElContenidoExcedeLaPagina() throws IOException {
        ContenidoPdf contenido = writer -> {
            for (int i = 0; i < 120; i++) {
                writer.escribirCampo("Linea", "Contenido de relleno para forzar el salto de pagina " + i);
            }
        };

        byte[] pdf = service.generar(contenido);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertTrue(document.getNumberOfPages() > 1,
                    "El contenido extenso debería producir más de una página");
        }
    }

    @Test
    void deberiaIncluirFechaGeneracionAunqueElContenidoNoLaEscriba() throws IOException {
        // El ContenidoPdf de prueba no escribe ninguna fecha: debe aportarla la base.
        ContenidoPdf contenido = writer -> writer.escribirCampo("Cuerpo", "Sin fecha propia");

        byte[] pdf = service.generar(contenido);
        String texto = extraerTexto(pdf);

        assertTrue(texto.matches("(?s).*\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}.*"),
                "El PDF debería contener la fecha/hora de generación aportada por la base");
    }

    private boolean esPdfValido(byte[] pdf) {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return document.getNumberOfPages() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private String extraerTexto(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
