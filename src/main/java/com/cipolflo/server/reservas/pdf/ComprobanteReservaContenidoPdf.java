package com.cipolflo.server.reservas.pdf;

import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.shared.pdf.ContenidoPdf;
import com.cipolflo.server.shared.pdf.PdfWriter;

import static com.cipolflo.server.shared.pdf.PdfFormato.booleano;
import static com.cipolflo.server.shared.pdf.PdfFormato.fecha;
import static com.cipolflo.server.shared.pdf.PdfFormato.hora;
import static com.cipolflo.server.shared.pdf.PdfFormato.importe;
import static com.cipolflo.server.shared.pdf.PdfFormato.texto;

/**
 * Cuerpo específico del comprobante de reserva en PDF.
 *
 * <p>Implementa {@link ContenidoPdf} recibiendo el {@link ReservaDetalleResponseDto}
 * ya resuelto (con cliente y servicio) y define el orden/contenido del comprobante
 * llamando a los métodos de alto nivel del {@link PdfWriter}. No maneja
 * {@code PDDocument}/páginas/streams ni escribe la fecha de generación: eso lo aporta
 * la base compartida {@code shared.pdf}.</p>
 */
public class ComprobanteReservaContenidoPdf implements ContenidoPdf {

    private final ReservaDetalleResponseDto detalle;

    public ComprobanteReservaContenidoPdf(ReservaDetalleResponseDto detalle) {
        this.detalle = detalle;
    }

    @Override
    public void escribir(PdfWriter writer) {
        writer.escribirTitulo("Comprobante de Reserva");
        writer.linea();

        escribirDatosReserva(writer);
        escribirCliente(writer);
        escribirServicio(writer);
        escribirNotas(writer);
    }

    private void escribirDatosReserva(PdfWriter writer) {
        writer.escribirSubtitulo("Datos generales");
        writer.escribirCampo("Tipo de reserva", texto(detalle.getTipoReserva()));
        writer.escribirCampo("Estado", texto(detalle.getEstado()));
        writer.escribirCampo("Fecha de entrada", fecha(detalle.getFechaEntrada()));
        writer.escribirCampo("Fecha de salida", fecha(detalle.getFechaSalida()));

        if (detalle.getHoraInicio() != null || detalle.getHoraFin() != null) {
            writer.escribirCampo("Horario",
                    hora(detalle.getHoraInicio()) + " - " + hora(detalle.getHoraFin()));
        }
        if(detalle.getCantidadTotal()!=null){
            writer.escribirCampo("Cantidad total", texto(detalle.getCantidadTotal()));
            writer.escribirCampo("Cantidad de menores", texto(detalle.getCantidadMenores()));
        }else{
            writer.escribirCampo("Cantidad", texto(detalle.getCantidad()));
        }
        writer.escribirCampo("Costo total", importe(detalle.getImporte()));
        writer.escribirCampo("Pago", booleano(detalle.getPago()));
        if(!detalle.getPago()){
            writer.escribirCampo("Saldo a pagar",importe(detalle.getMontoImpago()));
        }
        writer.espacio(8f);
    }

    private void escribirCliente(PdfWriter writer) {
        writer.escribirSubtitulo("Cliente");
        ClienteDetalleReservaDto cliente = detalle.getCliente();
        if (cliente != null) {
            writer.escribirCampo("Nombre", texto(cliente.nombre()));
            writer.escribirCampo("Documento", texto(cliente.cedula()));
            writer.escribirCampo("Tipo de cliente", texto(cliente.tipoCliente()));
        }
        writer.espacio(8f);
    }

    private void escribirServicio(PdfWriter writer) {
        writer.escribirSubtitulo("Servicio");
        ServicioDetalleReservaDto servicio = detalle.getServicio();
        if (servicio != null) {
            writer.escribirParrafo(texto(servicio.nombre())+" ("+texto(servicio.procedencia())+")");
        } else {
            writer.escribirParrafo("-");
        }
        writer.espacio(8f);
    }

    private void escribirNotas(PdfWriter writer) {
        writer.escribirSubtitulo("Notas");
        writer.escribirParrafo(
                detalle.getNotas() == null || detalle.getNotas().isBlank()
                        ? "Sin notas"
                        : detalle.getNotas());
    }
}
