package com.cipolflo.server.shared.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Implementación PDFBox de {@link IPdfGeneratorService}.
 *
 * <p>Centraliza el ciclo de vida del {@code PDDocument} (abrir, paginar vía
 * {@link PdfWriter}, cerrar, exportar a bytes) y escribe el encabezado común
 * —fecha/hora de generación— antes de delegar el cuerpo en el {@link ContenidoPdf}.
 * Así todos los comprobantes muestran la fecha de generación arriba sin que cada
 * documento tenga que repetirla.</p>
 */
@Service
public class PdfGeneratorService implements IPdfGeneratorService {

    @Override
    public byte[] generar(ContenidoPdf contenido) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(document);
            writer.escribirFechaGeneracion();
            contenido.escribir(writer);
            writer.cerrar();

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error al generar el documento PDF", e);
        }
    }
}
