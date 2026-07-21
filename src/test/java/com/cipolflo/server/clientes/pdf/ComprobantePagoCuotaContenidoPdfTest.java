package com.cipolflo.server.clientes.pdf;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.shared.pdf.PdfGeneratorService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComprobantePagoCuotaContenidoPdfTest {

    private final PdfGeneratorService generator = new PdfGeneratorService();

    @Test
    void deberiaArmarComprobanteSinExcepciones() {
        ComprobantePagoCuotaContenidoPdf contenido = new ComprobantePagoCuotaContenidoPdf(
                "Juan Pérez",
                List.of(YearMonth.of(2025, 11)),
                new BigDecimal("1500.00"),
                MetodoCobro.TRANSFERENCIA
        );

        byte[] pdf = assertDoesNotThrow(() -> generator.generar(contenido));

        assertTrue(pdf.length > 0);
    }

    @Test
    void deberiaIncluirElMensajeArmadoPorElFormatter() throws Exception {
        ComprobantePagoCuotaContenidoPdf contenido = new ComprobantePagoCuotaContenidoPdf(
                "Juan Pérez",
                List.of(YearMonth.of(2025, 11), YearMonth.of(2025, 12)),
                new BigDecimal("3000.00"),
                MetodoCobro.EFECTIVO
        );

        String texto = textoDe(generator.generar(contenido));

        assertTrue(texto.contains("Estimado/a Juan Pérez"), texto);
        assertTrue(texto.contains("Noviembre y Diciembre de 2025"), texto);
        assertTrue(texto.contains("$3000.00"), texto);
        assertTrue(texto.contains("Efectivo"), texto);
    }

    private String textoDe(byte[] pdf) throws Exception {
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(documento);
        }
    }
}