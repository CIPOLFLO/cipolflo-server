package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.service.IConsultaClienteDetalle;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.scheduled.ReporteSemanalReservasProperties;
import com.cipolflo.server.reservas.service.ReporteSemanalReservasService.ReservasSemana;
import com.cipolflo.server.servicios.service.IConsultaServicioSimple;
import com.cipolflo.server.shared.ZonaHoraria;
import com.cipolflo.server.shared.email.IEmailService;
import com.cipolflo.server.shared.email.SolicitudEmail;
import com.cipolflo.server.shared.email.TipoEventoEmail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica el reporte semanal: el reparto en las tres secciones ({@code categorizar}) y
 * el flujo de armado + envío ({@code enviarReporteSemanal}). La semana de referencia de
 * los tests de reparto es lunes 06/07/2026 a domingo 12/07/2026.
 */
@ExtendWith(MockitoExtension.class)
class ReporteSemanalReservasServiceTest {

    private static final LocalDate LUNES = LocalDate.of(2026, Month.JULY, 6);
    private static final LocalDate DOMINGO = LocalDate.of(2026, Month.JULY, 12);

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private IConsultaServicioSimple consultaServicioSimple;
    @Mock
    private IConsultaClienteDetalle consultaClienteDetalle;
    @Mock
    private IEmailService emailService;

    private ReporteSemanalReservasService servicioCon(String destinatario) {
        ReporteSemanalReservasProperties props =
                new ReporteSemanalReservasProperties(destinatario, "0 0 8 * * MON", "America/Montevideo");
        return new ReporteSemanalReservasService(
                reservaRepository, consultaServicioSimple, consultaClienteDetalle, emailService, props);
    }

    private Reserva reserva(EstadoReserva estado, Long clienteId, LocalDate entrada, LocalDate salida) {
        Reserva r = mock(Reserva.class);
        lenient().when(r.getEstado()).thenReturn(estado);
        lenient().when(r.getClienteId()).thenReturn(clienteId);
        lenient().when(r.getFechaEntrada()).thenReturn(entrada);
        lenient().when(r.getFechaSalida()).thenReturn(salida);
        return r;
    }

    // ---------- categorizar ----------

    @Test
    void enCurso_incluyeConfirmadasYEnCursoQueEmpezaronAntesYSiguenActivas() {
        Reserva confirmadaArrastre = reserva(EstadoReserva.CONFIRMADA, 1L,
                LocalDate.of(2026, Month.JULY, 2), LocalDate.of(2026, Month.JULY, 8));
        Reserva envuelveLaSemana = reserva(EstadoReserva.CONFIRMADA, 2L,
                LocalDate.of(2026, Month.JULY, 1), LocalDate.of(2026, Month.JULY, 20));
        Reserva estadoEnCurso = reserva(EstadoReserva.EN_CURSO, 3L,
                LocalDate.of(2026, Month.JULY, 3), LocalDate.of(2026, Month.JULY, 9));

        ReservasSemana resultado = ReporteSemanalReservasService.categorizar(
                List.of(confirmadaArrastre, envuelveLaSemana, estadoEnCurso), LUNES, DOMINGO);

        assertEquals(3, resultado.enCurso().size());
        assertTrue(resultado.enCurso().containsAll(List.of(confirmadaArrastre, envuelveLaSemana, estadoEnCurso)));
        assertTrue(resultado.confirmadas().isEmpty());
        assertTrue(resultado.pendientes().isEmpty());
    }

    @Test
    void confirmadas_incluyeConfirmadasQueInicianEnLaSemana() {
        Reserva iniciaMitadSemana = reserva(EstadoReserva.CONFIRMADA, 1L,
                LocalDate.of(2026, Month.JULY, 8), LocalDate.of(2026, Month.JULY, 15));
        Reserva enteraEnLaSemana = reserva(EstadoReserva.CONFIRMADA, 2L,
                LocalDate.of(2026, Month.JULY, 8), LocalDate.of(2026, Month.JULY, 10));
        Reserva iniciaElLunes = reserva(EstadoReserva.CONFIRMADA, 3L, LUNES, LUNES.plusDays(1));

        ReservasSemana resultado = ReporteSemanalReservasService.categorizar(
                List.of(iniciaMitadSemana, enteraEnLaSemana, iniciaElLunes), LUNES, DOMINGO);

        assertEquals(3, resultado.confirmadas().size());
        assertTrue(resultado.enCurso().isEmpty());
        assertTrue(resultado.pendientes().isEmpty());
    }

    @Test
    void pendientes_incluyePendientesQueInicianEnLaSemana() {
        Reserva pendienteEnSemana = reserva(EstadoReserva.PENDIENTE, 1L,
                LocalDate.of(2026, Month.JULY, 9), LocalDate.of(2026, Month.JULY, 11));
        Reserva pendienteInicioLunes = reserva(EstadoReserva.PENDIENTE, 2L, LUNES, DOMINGO);

        ReservasSemana resultado = ReporteSemanalReservasService.categorizar(
                List.of(pendienteEnSemana, pendienteInicioLunes), LUNES, DOMINGO);

        assertEquals(2, resultado.pendientes().size());
        assertTrue(resultado.enCurso().isEmpty());
        assertTrue(resultado.confirmadas().isEmpty());
    }

    @Test
    void descarta_reservasSinClienteYLasQueNoEncajanEnNingunaSeccion() {
        // Sin cliente (temporal): se ignora aunque inicie en la semana.
        Reserva sinCliente = reserva(EstadoReserva.CONFIRMADA, null,
                LocalDate.of(2026, Month.JULY, 8), LocalDate.of(2026, Month.JULY, 10));
        // Pendiente de arrastre (empezó antes): no inicia en la semana → fuera.
        Reserva pendienteArrastre = reserva(EstadoReserva.PENDIENTE, 1L,
                LocalDate.of(2026, Month.JULY, 1), LocalDate.of(2026, Month.JULY, 8));
        // Confirmada que arranca después del domingo → fuera.
        Reserva confirmadaFutura = reserva(EstadoReserva.CONFIRMADA, 2L,
                LocalDate.of(2026, Month.JULY, 13), LocalDate.of(2026, Month.JULY, 14));

        ReservasSemana resultado = ReporteSemanalReservasService.categorizar(
                List.of(sinCliente, pendienteArrastre, confirmadaFutura), LUNES, DOMINGO);

        assertTrue(resultado.enCurso().isEmpty());
        assertTrue(resultado.confirmadas().isEmpty());
        assertTrue(resultado.pendientes().isEmpty());
    }

    @Test
    void ordena_cadaSeccionPorFechaDeEntrada() {
        Reserva tarde = reserva(EstadoReserva.CONFIRMADA, 1L,
                LocalDate.of(2026, Month.JULY, 10), LocalDate.of(2026, Month.JULY, 11));
        Reserva temprano = reserva(EstadoReserva.CONFIRMADA, 2L,
                LocalDate.of(2026, Month.JULY, 7), LocalDate.of(2026, Month.JULY, 9));

        ReservasSemana resultado = ReporteSemanalReservasService.categorizar(
                List.of(tarde, temprano), LUNES, DOMINGO);

        assertEquals(List.of(temprano, tarde), resultado.confirmadas());
    }

    // ---------- enviarReporteSemanal ----------

    @Test
    void enviarReporteSemanal_sinDestinatario_noEnviaYReportaOmitido() {
        ReporteSemanalReservasService servicio = servicioCon("   ");

        String resumen = servicio.enviarReporteSemanal();

        assertTrue(resumen.toLowerCase().contains("omitido"));
        verify(emailService, never()).enviar(any());
        verify(reservaRepository, never()).findByEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                anyCollection(), any(), any());
    }

    @Test
    void enviarReporteSemanal_armaElCuerpoConServicioYClienteYEnvia() {
        // Semana actual real (la que usará el service internamente).
        LocalDate lunes = LocalDate.now(ZonaHoraria.URUGUAY)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Reserva "en curso": empezó antes del lunes y sigue activa → sección En curso.
        Reserva enCurso = reserva(EstadoReserva.CONFIRMADA, 20L, lunes.minusDays(2), lunes.plusDays(2));
        when(enCurso.getServicioId()).thenReturn(10L);

        when(reservaRepository.findByEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                anyCollection(), any(), any())).thenReturn(List.of(enCurso));
        when(consultaServicioSimple.getNombresByIds(anyCollection())).thenReturn(Map.of(10L, "Cabaña Norte"));
        when(consultaClienteDetalle.getDetallClienteSimple(eq(20L))).thenReturn(
                new ClienteDetalleReservaDto(20L, "Juan Pérez", "1.234.567-8", "099123456", "juan@mail.com", null));

        ReporteSemanalReservasService servicio = servicioCon("admin@cipolflo.com");
        String resumen = servicio.enviarReporteSemanal();

        ArgumentCaptor<SolicitudEmail> captor = ArgumentCaptor.forClass(SolicitudEmail.class);
        verify(emailService).enviar(captor.capture());
        SolicitudEmail enviado = captor.getValue();

        assertEquals("admin@cipolflo.com", enviado.destinatario());
        assertEquals(TipoEventoEmail.REPORTE_SEMANAL_RESERVAS, enviado.tipoEvento());
        assertFalse(enviado.html());
        assertTrue(enviado.cuerpo().contains("Cabaña Norte"), "debe incluir el nombre del servicio");
        assertTrue(enviado.cuerpo().contains("Juan Pérez"), "debe incluir el nombre del cliente");
        assertTrue(enviado.cuerpo().contains("099123456"), "debe incluir el teléfono del cliente");
        assertTrue(enviado.cuerpo().contains("EN CURSO"), "debe tener la sección En curso");
        assertTrue(resumen.contains("1 en curso"), "el resumen debe reflejar 1 reserva en curso");
    }
}
