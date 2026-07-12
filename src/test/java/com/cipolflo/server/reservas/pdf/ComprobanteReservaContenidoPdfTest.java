package com.cipolflo.server.reservas.pdf;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.shared.pdf.PdfGeneratorService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComprobanteReservaContenidoPdfTest {

    private final PdfGeneratorService generator = new PdfGeneratorService();

    @Test
    void deberiaArmarComprobanteConHorarioSinExcepciones() {
        ReservaDetalleResponseDto detalle = detalle(LocalTime.of(10, 0), LocalTime.of(12, 0));
        ComprobanteReservaContenidoPdf contenido = new ComprobanteReservaContenidoPdf(detalle);

        byte[] pdf = assertDoesNotThrow(() -> generator.generar(contenido));

        assertTrue(pdf.length > 0);
    }

    @Test
    void deberiaArmarComprobanteSinHorarioSinExcepciones() {
        // horaInicio/horaFin son opcionales.
        ReservaDetalleResponseDto detalle = detalle(null, null);
        ComprobanteReservaContenidoPdf contenido = new ComprobanteReservaContenidoPdf(detalle);

        byte[] pdf = assertDoesNotThrow(() -> generator.generar(contenido));

        assertTrue(pdf.length > 0);
    }

    @Test
    void deberiaEscribirLaCedulaComoDocumentoCuandoElClienteNoEsEmpresa() throws Exception {
        ReservaDetalleResponseDto detalle = detalle(null, null, clienteSocio());

        String texto = textoDe(generator.generar(new ComprobanteReservaContenidoPdf(detalle)));

        assertTrue(texto.matches("(?s).*Documento:\\s*12345678.*"), texto);
    }

    @Test
    void deberiaEscribirElRutComoDocumentoCuandoElClienteEsEmpresa() throws Exception {
        ReservaDetalleResponseDto detalle = detalle(null, null, clienteEmpresa());

        String texto = textoDe(generator.generar(new ComprobanteReservaContenidoPdf(detalle)));

        assertTrue(texto.matches("(?s).*RUT:\\s*211003420017.*"), texto);
        // El bug: sin RUT en el DTO, el documento de una empresa salía como "-".
        assertFalse(texto.matches("(?s).*(Documento|RUT):\\s*-.*"), texto);
    }

    private String textoDe(byte[] pdf) throws Exception {
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(documento);
        }
    }

    private ClienteDetalleReservaDto clienteSocio() {
        return new ClienteDetalleReservaDto(
                12L, "Juan Pérez", "12345678", null, "099111111", "juan@mail.com", TipoCliente.SOCIO);
    }

    private ClienteDetalleReservaDto clienteEmpresa() {
        return new ClienteDetalleReservaDto(
                20L, "Antel S.A.", null, "211003420017", "099222222", "empresa@mail.com", TipoCliente.EMPRESA);
    }

    private ReservaDetalleResponseDto detalle(LocalTime horaInicio, LocalTime horaFin) {
        return detalle(horaInicio, horaFin, clienteSocio());
    }

    private ReservaDetalleResponseDto detalle(LocalTime horaInicio, LocalTime horaFin,
                                              ClienteDetalleReservaDto cliente) {
        ServicioDetalleReservaDto servicio = new ServicioDetalleReservaDto(
                3L, "Cabaña del río", Procedencia.CAMPING, ModalidadPrecio.POR_DIA);

        return new ReservaDetalleResponseDto(
                42L,
                TipoReserva.COMUN,
                EstadoReserva.CONFIRMADA,
                Procedencia.CAMPING,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                horaInicio,
                horaFin,
                4, 1, null,
                BigDecimal.valueOf(15000),
                BigDecimal.ZERO,
                true,
                false, false, false,
                "Llegan a las 14hs",
                cliente, servicio,
                null, null, null, null);
    }
}
