package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.service.PagoCuotaService;
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.RegistroPagoReservaRequestDto;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.validators.PagoReservaValidator;
import com.cipolflo.server.reservas.validators.contexto.PagoReservaValidationContext;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private IFinanzaService finanzaService;

    @Mock
    private PagoReservaValidator pagoReservaValidator;

    @InjectMocks
    private PagoReservaService pagoReservaService;

    @Test
    void deberiaRegistrarPagoParcialYReducirMontoImpago() {
        Reserva reserva = crearReservaComun(BigDecimal.valueOf(1500));
        RegistroPagoReservaRequestDto dto = crearDto(BigDecimal.valueOf(500), false);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        pagoReservaService.registrarPago(1L, dto);

        assertFalse(reserva.getPago());
        assertEquals(BigDecimal.valueOf(1000), reserva.getMontoImpago());

        verify(pagoReservaValidator).validar(any(PagoReservaValidationContext.class));
        verify(finanzaService).registrarPagoReserva(any(FinanzaCrearRequestDto.class));
    }

    @Test
    void deberiaRegistrarPagoTotalYMarcarReservaComoPaga() {
        Reserva reserva = crearReservaComun(BigDecimal.valueOf(1500));
        RegistroPagoReservaRequestDto dto = crearDto(BigDecimal.valueOf(1500), true);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        pagoReservaService.registrarPago(1L, dto);

        assertTrue(reserva.getPago());
        assertEquals(BigDecimal.ZERO, reserva.getMontoImpago());

        verify(pagoReservaValidator).validar(any(PagoReservaValidationContext.class));
        verify(finanzaService).registrarPagoReserva(any(FinanzaCrearRequestDto.class));
    }

    @Test
    void deberiaConfirmarReservaAlPagarAlMenosLaMitadSiNoRequiereDocumentacion() {
        Reserva reserva = crearReservaComun(BigDecimal.valueOf(1500));
        RegistroPagoReservaRequestDto dto = crearDto(BigDecimal.valueOf(750), false);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        pagoReservaService.registrarPago(1L, dto);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void noDeberiaConfirmarSiRequiereDocumentacionYNoLaTiene() {
        Reserva reserva = crearReservaComunConDocumentacion(BigDecimal.valueOf(1500));
        RegistroPagoReservaRequestDto dto = crearDto(BigDecimal.valueOf(750), false);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        pagoReservaService.registrarPago(1L, dto);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void deberiaLanzarReservaNotFoundCuandoNoExiste() {
        RegistroPagoReservaRequestDto dto = crearDto(BigDecimal.valueOf(500), false);

        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ReservaNotFoundException.class,
                () -> pagoReservaService.registrarPago(99L, dto)
        );

        verify(pagoReservaValidator, never()).validar(any());
        verify(finanzaService, never()).registrarPagoReserva(any());
    }

    @Test
    void deberiaEnviarDatosCorrectosAFinanzaService() {
        Reserva reserva = crearReservaComun(BigDecimal.valueOf(1500));
        RegistroPagoReservaRequestDto dto = crearDto(BigDecimal.valueOf(500), false);
        dto.setNotas("Pago parcial");

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        pagoReservaService.registrarPago(1L, dto);

        verify(finanzaService).registrarPagoReserva(argThat(finanzaDto ->
                TipoMovimiento.INGRESO.equals(finanzaDto.getTipoMovimiento()) &&
                        Concepto.PAGO_RESERVA.equals(finanzaDto.getConcepto()) &&
                        Procedencia.CAMPING.equals(finanzaDto.getProcedencia()) &&
                        BigDecimal.valueOf(500).compareTo(finanzaDto.getImporte()) == 0 &&
                        FormaPago.EFECTIVO.equals(finanzaDto.getFormaPago()) &&
                        "Pago parcial".equals(finanzaDto.getNotas()) &&
                        Long.valueOf(1L).equals(finanzaDto.getReservaId())
        ));
    }

    private RegistroPagoReservaRequestDto crearDto(BigDecimal importe, boolean esPagoTotal) {
        RegistroPagoReservaRequestDto dto = new RegistroPagoReservaRequestDto();
        dto.setImporte(importe);
        dto.setEsPagoTotal(esPagoTotal);
        dto.setFormaPago(FormaPago.EFECTIVO);
        return dto;
    }

    private Reserva crearReservaComun(BigDecimal importe) {
        return Reserva.crear(
                TipoReserva.COMUN,
                1L,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
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
                importe,
                null
        );
    }

    private Reserva crearReservaComunConDocumentacion(BigDecimal importe) {
        return Reserva.crear(
                TipoReserva.COMUN,
                1L,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                false,
                importe,
                null
        );
    }
}
