package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.RegistroPagoReservaRequestDto;
import com.cipolflo.server.reservas.dto.ReservaFinalizacionCheckResponseDto;
import com.cipolflo.server.reservas.dto.ReservaFinalizacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.validators.FinalizacionReservaValidationContext;
import com.cipolflo.server.reservas.validators.FinalizacionReservaValidator;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalizacionReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private IPagoReservaService pagoReservaService;

    @Mock
    private FinalizacionReservaValidator finalizacionReservaValidator;

    @InjectMocks
    private FinalizacionReservaService service;

    @Test
    void verificarFinalizacionDeberiaRetornarFinalizableDirectamenteCuandoReservaEstaPaga() {
        Reserva reserva = crearReservaEnCursoPaga();

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        ReservaFinalizacionCheckResponseDto response = service.verificarFinalizacion(1L);

        assertTrue(response.puedeFinalizarSinPago());
        assertEquals(BigDecimal.ZERO, response.montoImpago());
    }

    @Test
    void verificarFinalizacionDeberiaRetornarSaldoPendienteCuandoReservaTieneMontoImpago() {
        Reserva reserva = crearReservaEnCursoConSaldoPendiente();

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        ReservaFinalizacionCheckResponseDto response = service.verificarFinalizacion(1L);

        assertFalse(response.puedeFinalizarSinPago());
        assertEquals(BigDecimal.valueOf(1500), response.montoImpago());
    }

    @Test
    void verificarFinalizacionDeberiaLanzarNotFoundCuandoReservaNoExiste() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ReservaNotFoundException.class,
                () -> service.verificarFinalizacion(99L)
        );
    }

    @Test
    void verificarFinalizacionDeberiaLanzarErrorCuandoReservaNoEstaEnCurso() {
        Reserva reserva = crearReservaConfirmada();

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> service.verificarFinalizacion(1L)
        );

        assertEquals(ReservaCodigoError.RESERVA_NO_FINALIZABLE.name(), exception.getCodigo());
    }

    @Test
    void finalizarDeberiaFinalizarReservaPagaSinRegistrarPago() {
        Reserva reserva = crearReservaEnCursoPaga();
        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.finalizar(1L, dto);

        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        verify(finalizacionReservaValidator).validar(any(FinalizacionReservaValidationContext.class));
        verify(pagoReservaService, never()).registrarPago(anyLong(), any());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void finalizarDeberiaRegistrarPagoYFinalizarCuandoTieneSaldoYCompletaPago() {
        Reserva reserva = crearReservaEnCursoConSaldoPendiente();

        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();
        dto.setCompletarPago(true);
        dto.setFormaPago(FormaPago.EFECTIVO);
        dto.setNotas("Pago al finalizar");

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.finalizar(1L, dto);

        ArgumentCaptor<RegistroPagoReservaRequestDto> captor =
                ArgumentCaptor.forClass(RegistroPagoReservaRequestDto.class);

        verify(pagoReservaService).registrarPago(eq(1L), captor.capture());

        RegistroPagoReservaRequestDto pagoDto = captor.getValue();

        assertEquals(BigDecimal.valueOf(1500), pagoDto.getImporte());
        // esPagoTotal debe ser false: se salda el monto impago sin sobrescribir el importe total
        assertFalse(pagoDto.getEsPagoTotal());
        assertEquals(FormaPago.EFECTIVO, pagoDto.getFormaPago());
        assertEquals("Pago al finalizar", pagoDto.getNotas());

        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void finalizarDeberiaFinalizarSinRegistrarPagoCuandoTieneSaldoYNoCompletaPago() {
        Reserva reserva = crearReservaEnCursoConSaldoPendiente();

        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();
        dto.setCompletarPago(false);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        service.finalizar(1L, dto);

        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertEquals(BigDecimal.valueOf(1500), reserva.getMontoImpago());

        verify(pagoReservaService, never()).registrarPago(anyLong(), any());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void finalizarDeberiaLanzarNotFoundCuandoReservaNoExiste() {
        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();

        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ReservaNotFoundException.class,
                () -> service.finalizar(99L, dto)
        );

        verify(reservaRepository, never()).save(any());
    }

    private Reserva crearReservaConfirmada() {
        return Reserva.crear(
                TipoReserva.COMUN,
                1L,
                1L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                BigDecimal.valueOf(2000),
                null
        );
    }

    private Reserva crearReservaEnCursoPaga() {
        Reserva reserva = crearReservaConfirmada();
        reserva.registrarPago(BigDecimal.valueOf(2000), true);
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        return reserva;
    }

    private Reserva crearReservaEnCursoConSaldoPendiente() {
        Reserva reserva = crearReservaConfirmada();
        reserva.registrarPago(BigDecimal.valueOf(500), false);
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        return reserva;
    }
}