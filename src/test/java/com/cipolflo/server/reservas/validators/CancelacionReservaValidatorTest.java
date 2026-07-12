package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.PagoAsociadoReservaDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.contexto.CancelacionReservaValidationContext;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancelacionReservaValidatorTest {

    private final CancelacionReservaValidator validator = new CancelacionReservaValidator();

    @Test
    void noDeberiaLanzarExcepcionCuandoReservaSinPagosEsValida() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.PENDIENTE),
                List.of(),
                null,
                null,
                null
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void noDeberiaLanzarExcepcionCuandoReservaConPagosNoGeneraDevolucion() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CONFIRMADA),
                List.of(crearPago()),
                false,
                null,
                null
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void noDeberiaLanzarExcepcionCuandoReservaConPagosGeneraDevolucionConFormaPagoEImporteValido() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CONFIRMADA),
                List.of(crearPago()),
                true,
                FormaPago.EFECTIVO,
                BigDecimal.valueOf(500)
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void deberiaLanzarErrorCuandoReservaEstaFinalizada() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.FINALIZADA),
                List.of(),
                null,
                null,
                null
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.RESERVA_NO_CANCELABLE.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoReservaEstaCancelada() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CANCELADA),
                List.of(),
                null,
                null,
                null
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.RESERVA_NO_CANCELABLE.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoReservaEstaEnCurso() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.EN_CURSO),
                List.of(),
                null,
                null,
                null
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.RESERVA_NO_CANCELABLE.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoTienePagosYSinDecisionDeDevolucion() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CONFIRMADA),
                List.of(crearPago()),
                null,
                null,
                null
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.DECISION_DEVOLUCION_REQUERIDA.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoGeneraDevolucionSinFormaPago() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CONFIRMADA),
                List.of(crearPago()),
                true,
                null,
                BigDecimal.valueOf(500)
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(
                ReservaCodigoError.FORMA_PAGO_REQUERIDA_PARA_DEVOLUCION.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaLanzarErrorCuandoGeneraDevolucionSinImporte() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CONFIRMADA),
                List.of(crearPago()),
                true,
                FormaPago.EFECTIVO,
                null
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.IMPORTE_DEVOLUCION_INVALIDO.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoImporteDevolucionEsCero() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CONFIRMADA),
                List.of(crearPago()),
                true,
                FormaPago.EFECTIVO,
                BigDecimal.ZERO
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.IMPORTE_DEVOLUCION_INVALIDO.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoImporteDevolucionEsNegativo() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CONFIRMADA),
                List.of(crearPago()),
                true,
                FormaPago.EFECTIVO,
                BigDecimal.valueOf(-1)
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.IMPORTE_DEVOLUCION_INVALIDO.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoImporteDevolucionSuperaTotalPagado() {
        CancelacionReservaValidationContext context = crearContext(
                crearReserva(EstadoReserva.CONFIRMADA),
                List.of(crearPago()),
                true,
                FormaPago.EFECTIVO,
                BigDecimal.valueOf(1500)
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(
                ReservaCodigoError.IMPORTE_DEVOLUCION_SUPERA_TOTAL_PAGADO.name(),
                exception.getCodigo()
        );
    }

    private CancelacionReservaValidationContext crearContext(
            Reserva reserva,
            List<PagoAsociadoReservaDto> pagosAsociados,
            Boolean generarDevolucion,
            FormaPago formaPago,
            BigDecimal importeDevolucion
    ) {
        ReservaCancelacionRequestDto dto = new ReservaCancelacionRequestDto();
        dto.setGenerarDevolucion(generarDevolucion);
        dto.setFormaPago(formaPago);
        dto.setImporteDevolucion(importeDevolucion);

        BigDecimal importeTotalPagos = pagosAsociados.stream()
                .map(PagoAsociadoReservaDto::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CancelacionReservaValidationContext(
                dto,
                reserva,
                pagosAsociados,
                importeTotalPagos
        );
    }

    private Reserva crearReserva(EstadoReserva estadoReserva) {
        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                1L,
                1L,
                Procedencia.SEDE,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                true,
                BigDecimal.valueOf(1000),
                null
        );

        if (estadoReserva == EstadoReserva.CONFIRMADA) {
            reserva.registrarPago(BigDecimal.valueOf(500), false);
        }

        if (estadoReserva == EstadoReserva.EN_CURSO) {
            reserva.registrarPago(BigDecimal.valueOf(500), false);
            reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        }

        if (estadoReserva == EstadoReserva.FINALIZADA) {
            reserva.registrarPago(BigDecimal.valueOf(500), false);
            reserva.cambiarEstado(EstadoReserva.EN_CURSO);
            reserva.cambiarEstado(EstadoReserva.FINALIZADA);
        }

        if (estadoReserva == EstadoReserva.CANCELADA) {
            reserva.cancelar();
        }

        return reserva;
    }

    private PagoAsociadoReservaDto crearPago() {
        return new PagoAsociadoReservaDto(
                1L,
                LocalDate.of(2026, 7, 1),
                BigDecimal.valueOf(1000),
                FormaPago.EFECTIVO
        );
    }
}