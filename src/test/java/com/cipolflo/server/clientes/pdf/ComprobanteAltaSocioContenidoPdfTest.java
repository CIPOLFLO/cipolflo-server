package com.cipolflo.server.clientes.pdf;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.shared.pdf.PdfGeneratorService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComprobanteAltaSocioContenidoPdfTest {

    private final PdfGeneratorService generator = new PdfGeneratorService();

    @Test
    void deberiaArmarComprobanteSinExcepciones() {
        Socio socio = socio("Juan Pérez", 42);
        ComprobanteAltaSocioContenidoPdf contenido = new ComprobanteAltaSocioContenidoPdf(socio);

        byte[] pdf = assertDoesNotThrow(() -> generator.generar(contenido));

        assertTrue(pdf.length > 0);
    }

    @Test
    void deberiaIncluirNombreYNumeroDeSocioEnElMensaje() throws Exception {
        Socio socio = socio("Juan Pérez", 42);

        String texto = textoDe(generator.generar(new ComprobanteAltaSocioContenidoPdf(socio)));

        assertTrue(texto.contains("Estimado/a Juan Pérez"), texto);
        assertTrue(texto.contains("n° 42"), texto);
    }

    private String textoDe(byte[] pdf) throws Exception {
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(documento);
        }
    }

    private Socio socio(String nombreCompleto, Integer numeroSocio) {
        Socio socio = new Socio();
        socio.setNombreCompleto(nombreCompleto);
        socio.setNumeroSocio(numeroSocio);
        socio.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        socio.setEstado(EstadoSocio.ACTIVO);
        socio.setPais("Uruguay");
        socio.setDepartamento("Montevideo");
        socio.setCiudad("Montevideo");
        socio.setFechaIngreso(LocalDate.of(2026, 7, 16));
        socio.setMetodoCobro(MetodoCobro.TRANSFERENCIA);
        return socio;
    }
}