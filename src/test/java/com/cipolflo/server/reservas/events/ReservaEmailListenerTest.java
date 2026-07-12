package com.cipolflo.server.reservas.events;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.shared.email.EmailException;
import com.cipolflo.server.shared.email.IEmailService;
import com.cipolflo.server.shared.email.SolicitudEmail;
import com.cipolflo.server.shared.email.TipoEventoEmail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaEmailListenerTest {

    @Mock
    private IReservaService reservaService;

    @Mock
    private IEmailService emailService;

    @InjectMocks
    private ReservaEmailListener listener;

    private ReservaDetalleResponseDto detalleCon(ClienteDetalleReservaDto cliente) {
        return new ReservaDetalleResponseDto(
                10L, null, null, null,
                null, null, null, null,
                null, null, null,
                null, null, null,
                null, null, null,
                null,
                cliente, null,
                null, null, null, null);
    }

    private ClienteDetalleReservaDto cliente(String nombre, String email) {
        return new ClienteDetalleReservaDto(1L, nombre, "1.234.567-8", null, "099123456", email, TipoCliente.PARTICULAR);
    }

    @Test
    void conEmail_enviaConElContextoCorrecto() {
        when(reservaService.getDetalle(10L)).thenReturn(detalleCon(cliente("Juan", "juan@mail.com")));

        listener.onReservaCreada(new ReservaCreadaEvent(10L));

        ArgumentCaptor<SolicitudEmail> captor = ArgumentCaptor.forClass(SolicitudEmail.class);
        verify(emailService).enviar(captor.capture());
        SolicitudEmail solicitud = captor.getValue();
        assertEquals("juan@mail.com", solicitud.destinatario());
        assertEquals(TipoEventoEmail.RESERVA_CREADA, solicitud.tipoEvento());
        assertEquals(10L, solicitud.referenciaId());
    }

    @Test
    void sinCliente_noEnvia() {
        when(reservaService.getDetalle(10L)).thenReturn(detalleCon(null));

        listener.onReservaCreada(new ReservaCreadaEvent(10L));

        verify(emailService, never()).enviar(any());
    }

    @Test
    void emailEnBlanco_noEnvia() {
        when(reservaService.getDetalle(10L)).thenReturn(detalleCon(cliente("Juan", "  ")));

        listener.onReservaCreada(new ReservaCreadaEvent(10L));

        verify(emailService, never()).enviar(any());
    }

    @Test
    void emailNull_noEnvia() {
        when(reservaService.getDetalle(10L)).thenReturn(detalleCon(cliente("Juan", null)));

        listener.onReservaCreada(new ReservaCreadaEvent(10L));

        verify(emailService, never()).enviar(any());
    }

    @Test
    void nombreNull_enviaIgual() {
        when(reservaService.getDetalle(10L)).thenReturn(detalleCon(cliente(null, "juan@mail.com")));

        listener.onReservaCreada(new ReservaCreadaEvent(10L));

        verify(emailService).enviar(any(SolicitudEmail.class));
    }

    @Test
    void nombreEnBlanco_enviaIgual() {
        when(reservaService.getDetalle(10L)).thenReturn(detalleCon(cliente("   ", "juan@mail.com")));

        listener.onReservaCreada(new ReservaCreadaEvent(10L));

        verify(emailService).enviar(any(SolicitudEmail.class));
    }

    @Test
    void envioFalla_noPropagaLaExcepcion() {
        when(reservaService.getDetalle(10L)).thenReturn(detalleCon(cliente("Juan", "juan@mail.com")));
        doThrow(new EmailException("SMTP caído", null)).when(emailService).enviar(any(SolicitudEmail.class));

        assertDoesNotThrow(() -> listener.onReservaCreada(new ReservaCreadaEvent(10L)));
    }
}