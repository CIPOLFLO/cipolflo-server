package com.cipolflo.server.reservas.service;

import com.cipolflo.server.finanzas.service.IConsultaPagosAsociadosReserva;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.PagoAsociadoReservaDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionCheckResponseDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.validators.CancelacionReservaValidator;
import com.cipolflo.server.reservas.validators.contexto.CancelacionReservaValidationContext;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelacionReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private IConsultaPagosAsociadosReserva consultaPagosAsociadosReserva;

    @Mock
    private IFinanzaService finanzaService;

    @Mock
    private CancelacionReservaValidator cancelacionReservaValidator;

    @InjectMocks
    private CancelacionReservaService service;

    @Test
    void verificarCancelacionDeberiaRetornarCancelacionDirectaCuandoNoTienePagos() {
        Reserva reserva = crearReservaPendiente();

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaPagosAsociadosReserva.getPagosAsociados(1L)).thenReturn(List.of());

        ReservaCancelacionCheckResponseDto response = service.verificarCancelacion(1L);

        assertTrue(response.puedeCancelarseDirectamente());
        assertTrue(response.pagosAsociados().isEmpty());
        assertEquals(BigDecimal.ZERO, response.importeTotalPagos());
    }

    @Test
    void verificarCancelacionDeberiaRetornarPagosAsociadosCuandoTienePagos() {
        Reserva reserva = crearReservaConfirmada();
        List<PagoAsociadoReservaDto> pagos = List.of(
                crearPago(1L, BigDecimal.valueOf(500)),
                crearPago(2L, BigDecimal.valueOf(700))
        );

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaPagosAsociadosReserva.getPagosAsociados(1L)).thenReturn(pagos);

        ReservaCancelacionCheckResponseDto response = service.verificarCancelacion(1L);

        assertFalse(response.puedeCancelarseDirectamente());
        assertEquals(2, response.pagosAsociados().size());
        assertEquals(BigDecimal.valueOf(1200), response.importeTotalPagos());
    }

    @Test
    void verificarCancelacionDeberiaLanzarNotFoundCuandoReservaNoExiste() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservaNotFoundException.class, () -> service.verificarCancelacion(99L));

        verify(consultaPagosAsociadosReserva, never()).getPagosAsociados(any());
    }

    @Test
    void verificarCancelacionDeberiaLanzarErrorCuandoReservaNoEsCancelable() {
        Reserva reserva = crearReservaFinalizada();

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> service.verificarCancelacion(1L)
        );

        assertEquals(ReservaCodigoError.RESERVA_NO_CANCELABLE.name(), exception.getCodigo());
        verify(consultaPagosAsociadosReserva, never()).getPagosAsociados(any());
    }

    @Test
    void cancelarSinPagosDeberiaMarcarReservaComoCancelada() {
        Reserva reserva = crearReservaPendiente();
        ReservaCancelacionRequestDto dto = crearDto(false, null);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaPagosAsociadosReserva.getPagosAsociados(1L)).thenReturn(List.of());

        service.cancelar(1L, dto);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());

        verify(cancelacionReservaValidator).validar(any(CancelacionReservaValidationContext.class));
        verify(reservaRepository).save(reserva);
        verify(reservaRepository, never()).delete(any(Reserva.class));
        verify(finanzaService, never()).registrarDevolucionPorCancelacionReserva(any(), any(), any(), any());
    }

    @Test
    void cancelarConPagosYDevolucionDeberiaRegistrarDevolucionYCancelarReserva() {
        Reserva reserva = crearReservaConfirmada();
        ReservaCancelacionRequestDto dto = crearDto(true, FormaPago.EFECTIVO);
        List<PagoAsociadoReservaDto> pagos = List.of(crearPago(1L, BigDecimal.valueOf(500)));

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaPagosAsociadosReserva.getPagosAsociados(1L)).thenReturn(pagos);

        service.cancelar(1L, dto);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());

        verify(cancelacionReservaValidator).validar(any(CancelacionReservaValidationContext.class));
        verify(finanzaService).registrarDevolucionPorCancelacionReserva(
                1L,
                dto.getImporteDevolucion(),
                FormaPago.EFECTIVO,
                reserva.getProcedencia()
        );
        verify(reservaRepository).save(reserva);
        verify(reservaRepository, never()).delete(any(Reserva.class));
    }

    @Test
    void cancelarConPagosSinDevolucionDeberiaCancelarReservaSinRegistrarDevolucion() {
        Reserva reserva = crearReservaConfirmada();
        ReservaCancelacionRequestDto dto = crearDto(false, null);
        List<PagoAsociadoReservaDto> pagos = List.of(crearPago(1L, BigDecimal.valueOf(500)));

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaPagosAsociadosReserva.getPagosAsociados(1L)).thenReturn(pagos);

        service.cancelar(1L, dto);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());

        verify(cancelacionReservaValidator).validar(any(CancelacionReservaValidationContext.class));
        verify(finanzaService, never()).registrarDevolucionPorCancelacionReserva(any(), any(), any(), any());
        verify(reservaRepository).save(reserva);
        verify(reservaRepository, never()).delete(any(Reserva.class));
    }

    @Test
    void cancelarDeberiaLanzarNotFoundCuandoReservaNoExiste() {
        ReservaCancelacionRequestDto dto = crearDto(false, null);

        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservaNotFoundException.class, () -> service.cancelar(99L, dto));

        verify(consultaPagosAsociadosReserva, never()).getPagosAsociados(any());
        verify(cancelacionReservaValidator, never()).validar(any());
    }

    @Test
    void cancelarDeberiaInvocarValidadorAntesDeModificarReserva() {
        Reserva reserva = crearReservaConfirmada();
        ReservaCancelacionRequestDto dto = crearDto(false, null);
        List<PagoAsociadoReservaDto> pagos = List.of(crearPago(1L, BigDecimal.valueOf(500)));

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaPagosAsociadosReserva.getPagosAsociados(1L)).thenReturn(pagos);

        service.cancelar(1L, dto);

        InOrder inOrder = inOrder(cancelacionReservaValidator, reservaRepository);

        inOrder.verify(cancelacionReservaValidator).validar(any(CancelacionReservaValidationContext.class));
        inOrder.verify(reservaRepository).save(reserva);
    }

    private Reserva crearReservaPendiente() {
        return Reserva.crear(
                TipoReserva.COMUN,
                1L,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                false,
                BigDecimal.valueOf(1500),
                null
        );
    }

    private Reserva crearReservaConfirmada() {
        return Reserva.crear(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO,
                null,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                null,
                null,
                null,
                null,
                null,
                
                null,
                false,
                false,
                BigDecimal.ZERO,
                null
        );
    }

    private Reserva crearReservaFinalizada() {
        Reserva reserva = crearReservaConfirmada();
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        reserva.cambiarEstado(EstadoReserva.FINALIZADA);
        return reserva;
    }

    private PagoAsociadoReservaDto crearPago(Long id, BigDecimal importe) {
        return new PagoAsociadoReservaDto(
                id,
                LocalDate.now(),
                importe,
                FormaPago.EFECTIVO
        );
    }

    private ReservaCancelacionRequestDto crearDto(Boolean generarDevolucion, FormaPago formaPago) {
        ReservaCancelacionRequestDto dto = new ReservaCancelacionRequestDto();
        dto.setGenerarDevolucion(generarDevolucion);
        dto.setFormaPago(formaPago);

        if (Boolean.TRUE.equals(generarDevolucion)) {
            dto.setImporteDevolucion(BigDecimal.valueOf(500));
        }

        return dto;
    }
}