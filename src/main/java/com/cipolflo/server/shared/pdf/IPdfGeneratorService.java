package com.cipolflo.server.shared.pdf;

/**
 * Servicio compartido de generación de PDF. Se encarga del ciclo de vida del
 * documento y del encabezado común; cada documento aporta su propio cuerpo vía
 * {@link ContenidoPdf}. Mismo rol que {@code IExportService} pero para PDF.
 */
public interface IPdfGeneratorService {

    /**
     * Genera un PDF: crea el documento, escribe el encabezado común (fecha/hora de
     * generación), invoca {@code contenido.escribir(writer)} para el cuerpo específico
     * y devuelve los bytes del PDF resultante.
     *
     * @param contenido cuerpo específico del documento a generar
     * @return el PDF serializado en bytes
     */
    byte[] generar(ContenidoPdf contenido);
}
