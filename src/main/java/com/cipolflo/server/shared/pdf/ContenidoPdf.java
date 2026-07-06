package com.cipolflo.server.shared.pdf;

/**
 * Contrato que implementa cada documento PDF para dibujar su propio contenido.
 *
 * <p>Patrón Strategy/Template Method: la base compartida ({@code PdfGeneratorService})
 * se encarga del ciclo de vida del documento (abrir, paginar, cerrar, exportar a bytes)
 * y del encabezado común (fecha/hora de generación); cada tipo de documento
 * —por ejemplo el comprobante de reserva— solo aporta el cuerpo específico
 * implementando esta interfaz, sin repetir el manejo de {@code PDDocument}/streams.</p>
 */
@FunctionalInterface
public interface ContenidoPdf {

    /**
     * Escribe el cuerpo específico del documento usando los métodos de alto nivel
     * del {@link PdfWriter}.
     *
     * @param writer helper que centraliza coordenadas, fuentes, márgenes y saltos de página
     */
    void escribir(PdfWriter writer);
}
