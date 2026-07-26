package com.cipolflo.server.reservas.events;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.reservas.scheduled.ReporteSemanalReservasProperties;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.shared.email.IEmailService;
import com.cipolflo.server.shared.email.SolicitudEmail;
import com.cipolflo.server.shared.email.TipoEventoEmail;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaCancelacionEmailListenerTest {

    @Mock
    private IReservaService reservaService;

    @Mock
    private IEmailService emailService;

    private ReservaCancelacionEmailListener listenerCon(String destinatarioAdmin) {
        ReporteSemanalReservasProperties props = new ReporteSemanalReservasProperties(destinatarioAdmin, null, null);
        return new ReservaCancelacionEmailListener(reservaService, emailService, props);
    }

    private ReservaDetalleResponseDto detalle(
            Long id,
            ClienteDetalleReservaDto cliente,
            String nombreServicio,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            String updatedBy
    ) {
        ServicioDetalleReservaDto servicio = new ServicioDetalleReservaDto(
                1L,
                nombreServicio,
                Procedencia.CAMPING
        );

        return new ReservaDetalleResponseDto(
                id, null, null, null,
                fechaEntrada, fechaSalida, null, null,
                null, null, null,
                null, null, null,
                null, null, null,
                null,null,  null,
                cliente, servicio, null, null,
                null, updatedBy
        );
    }

    private ClienteDetalleReservaDto cliente(String nombre, String email) {
        return new ClienteDetalleReservaDto(1L, nombre, "1.234.567-8", null, "099123456", email, TipoCliente.PARTICULAR);
    }

    @Test
    void manual_enviaMailAlSocioYAAdministracionConResponsable() {
        ReservaCancelacionEmailListener listener = listenerCon("admin@cipolflo.com");
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        ReservaDetalleResponseDto detalle = detalle(10L, cliente("Juan", "juan@mail.com"), "Cabaña",
                fecha, fecha.plusDays(2), "admin@cipolflo.com");
        when(reservaService.getDetalle(10L)).thenReturn(detalle);

        listener.onReservaCancelada(ReservaCanceladaEvent.manual(10L));

        ArgumentCaptor<SolicitudEmail> captor = ArgumentCaptor.forClass(SolicitudEmail.class);
        verify(emailService, times(2)).enviar(captor.capture());

        SolicitudEmail mailSocio = captor.getAllValues().get(0);
        assertEquals("juan@mail.com", mailSocio.destinatario());
        assertTrue(mailSocio.cuerpo().contains("Estimado/a Juan"));
        assertTrue(mailSocio.cuerpo().contains("para el período 01/06/2026 - 03/06/2026"));
        assertFalse(mailSocio.cuerpo().contains("deshabilitado"));

        SolicitudEmail mailAdmin = captor.getAllValues().get(1);
        assertEquals("admin@cipolflo.com", mailAdmin.destinatario());
        assertTrue(mailAdmin.cuerpo().contains("Juan"));
        assertTrue(mailAdmin.cuerpo().contains("Cabaña"));
        assertTrue(mailAdmin.cuerpo().contains("realizada por admin@cipolflo.com"));
        assertEquals(TipoEventoEmail.RESERVA_CANCELADA, mailAdmin.tipoEvento());
    }

    @Test
    void mismoDia_indicaDiaEnVezDePeriodo() {
        ReservaCancelacionEmailListener listener = listenerCon("admin@cipolflo.com");
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        ReservaDetalleResponseDto detalle = detalle(10L, cliente("Juan", "juan@mail.com"), "Cancha",
                fecha, fecha, null);
        when(reservaService.getDetalle(10L)).thenReturn(detalle);

        listener.onReservaCancelada(ReservaCanceladaEvent.manual(10L));

        ArgumentCaptor<SolicitudEmail> captor = ArgumentCaptor.forClass(SolicitudEmail.class);
        verify(emailService, times(2)).enviar(captor.capture());

        assertTrue(captor.getAllValues().get(0).cuerpo().contains("para el día 01/06/2026"));
    }

    @Test
    void porInhabilitacionServicio_socioRecibeMotivoYAdministracionUnMailConsolidado() {
        ReservaCancelacionEmailListener listener = listenerCon("admin@cipolflo.com");
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        ReservaDetalleResponseDto detalle1 = detalle(1L, cliente("Juan", "juan@mail.com"), "Cabaña",
                fecha, fecha.plusDays(1), null);
        ReservaDetalleResponseDto detalle2 = detalle(2L, cliente("Ana", "ana@mail.com"), "Cabaña",
                fecha.plusDays(5), fecha.plusDays(6), null);
        when(reservaService.getDetalle(1L)).thenReturn(detalle1);
        when(reservaService.getDetalle(2L)).thenReturn(detalle2);

        listener.onReservaCancelada(new ReservaCanceladaEvent(
                List.of(1L, 2L), MotivoCancelacionReserva.INHABILITACION_SERVICIO));

        ArgumentCaptor<SolicitudEmail> captor = ArgumentCaptor.forClass(SolicitudEmail.class);
        verify(emailService, times(3)).enviar(captor.capture());

        SolicitudEmail mailSocio1 = captor.getAllValues().get(0);
        assertTrue(mailSocio1.cuerpo().contains("el servicio Cabaña ha sido deshabilitado temporalmente"));

        SolicitudEmail mailAdmin = captor.getAllValues().get(2);
        assertEquals("admin@cipolflo.com", mailAdmin.destinatario());
        assertTrue(mailAdmin.cuerpo().contains("Se deshabilitó el servicio Cabaña"));
        assertTrue(mailAdmin.cuerpo().contains("Juan"));
        assertTrue(mailAdmin.cuerpo().contains("Ana"));
        assertFalse(mailAdmin.cuerpo().contains("#1"));
    }

    @Test
    void porBajaDeSocio_administracionRecibeUnMailConsolidadoConNombreDelSocioUnaSolaVez() {
        ReservaCancelacionEmailListener listener = listenerCon("admin@cipolflo.com");
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        ClienteDetalleReservaDto socio = cliente("Juan", "juan@mail.com");
        ReservaDetalleResponseDto detalle1 = detalle(1L, socio, "Cabaña", fecha, fecha.plusDays(1), null);
        ReservaDetalleResponseDto detalle2 = detalle(2L, socio, "Cancha", fecha.plusDays(5), fecha.plusDays(5), null);
        when(reservaService.getDetalle(1L)).thenReturn(detalle1);
        when(reservaService.getDetalle(2L)).thenReturn(detalle2);

        listener.onReservaCancelada(new ReservaCanceladaEvent(
                List.of(1L, 2L), MotivoCancelacionReserva.BAJA_SOCIO));

        ArgumentCaptor<SolicitudEmail> captor = ArgumentCaptor.forClass(SolicitudEmail.class);
        verify(emailService, times(3)).enviar(captor.capture());

        SolicitudEmail mailAdmin = captor.getAllValues().get(2);
        assertTrue(mailAdmin.cuerpo().contains("Se dio de baja al socio Juan"));
        assertTrue(mailAdmin.cuerpo().contains("Cabaña"));
        assertTrue(mailAdmin.cuerpo().contains("Cancha"));
    }

    @Test
    void porVencimientoDePlazoDeConfirmacion_socioRecibeMotivoYAdministracionUnMailConsolidado() {
        ReservaCancelacionEmailListener listener = listenerCon("admin@cipolflo.com");
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        ReservaDetalleResponseDto detalle1 = detalle(1L, cliente("Juan", "juan@mail.com"), "Cabaña",
                fecha, fecha.plusDays(1), null);
        ReservaDetalleResponseDto detalle2 = detalle(2L, cliente("Ana", "ana@mail.com"), "Cancha",
                fecha.plusDays(5), fecha.plusDays(5), null);
        when(reservaService.getDetalle(1L)).thenReturn(detalle1);
        when(reservaService.getDetalle(2L)).thenReturn(detalle2);

        listener.onReservaCancelada(new ReservaCanceladaEvent(
                List.of(1L, 2L), MotivoCancelacionReserva.VENCIMIENTO_PLAZO_CONFIRMACION));

        ArgumentCaptor<SolicitudEmail> captor = ArgumentCaptor.forClass(SolicitudEmail.class);
        verify(emailService, times(3)).enviar(captor.capture());

        SolicitudEmail mailSocio1 = captor.getAllValues().get(0);
        assertTrue(mailSocio1.cuerpo().contains("no fue confirmada dentro del plazo establecido"));

        SolicitudEmail mailAdmin = captor.getAllValues().get(2);
        assertEquals("admin@cipolflo.com", mailAdmin.destinatario());
        assertTrue(mailAdmin.cuerpo().contains("no haber sido confirmadas"));
        assertTrue(mailAdmin.cuerpo().contains("Juan"));
        assertTrue(mailAdmin.cuerpo().contains("Cabaña"));
        assertTrue(mailAdmin.cuerpo().contains("Ana"));
        assertTrue(mailAdmin.cuerpo().contains("Cancha"));
    }

    @Test
    void sinDestinatarioDeAdministracion_noEnviaMailAAdministracion() {
        ReservaCancelacionEmailListener listener = listenerCon("");
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        when(reservaService.getDetalle(10L)).thenReturn(
                detalle(10L, cliente("Juan", "juan@mail.com"), "Cabaña", fecha, fecha, null));

        listener.onReservaCancelada(ReservaCanceladaEvent.manual(10L));

        verify(emailService, times(1)).enviar(any(SolicitudEmail.class));
    }

    @Test
    void sinEmailDeCliente_noEnviaMailAlSocioPeroSiAAdministracion() {
        ReservaCancelacionEmailListener listener = listenerCon("admin@cipolflo.com");
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        when(reservaService.getDetalle(10L)).thenReturn(
                detalle(10L, cliente("Juan", null), "Cabaña", fecha, fecha, null));

        listener.onReservaCancelada(ReservaCanceladaEvent.manual(10L));

        verify(emailService, times(1)).enviar(any(SolicitudEmail.class));
    }

    @Test
    void listaVacia_noEnviaNingunMail() {
        ReservaCancelacionEmailListener listener = listenerCon("admin@cipolflo.com");

        listener.onReservaCancelada(new ReservaCanceladaEvent(List.of(), MotivoCancelacionReserva.MANUAL));

        verify(emailService, never()).enviar(any());
        verify(reservaService, never()).getDetalle(any());
    }
}
