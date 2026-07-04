package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.PagoAsociadoReservaDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.contexto.CancelacionReservaValidationContext;
import com.cipolflo.server.shared.enums.FormaPago;
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
                EstadoReserva.PENDIENTE,
                List.of(),
                null,
                null
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void noDeberiaLanzarExcepcionCuandoReservaConPagosNoGeneraDevolucion() {
        CancelacionReservaValidationContext context = crearContext(
                EstadoReserva.CONFIRMADA,
                List.of(crearPago()),
                false,
                null
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void noDeberiaLanzarExcepcionCuandoReservaConPagosGeneraDevolucionConFormaPago() {
        CancelacionReservaValidationContext context = crearContext(
                EstadoReserva.CONFIRMADA,
                List.of(crearPago()),
                true,
                FormaPago.EFECTIVO
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void deberiaLanzarErrorCuandoReservaEstaFinalizada() {
        CancelacionReservaValidationContext context = crearContext(
                EstadoReserva.FINALIZADA,
                List.of(),
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
                EstadoReserva.CANCELADA,
                List.of(),
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
                EstadoReserva.CONFIRMADA,
                List.of(crearPago()),
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
                EstadoReserva.CONFIRMADA,
                List.of(crearPago()),
                true,
                null
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

    private CancelacionReservaValidationContext crearContext(
            EstadoReserva estadoReserva,
            List<PagoAsociadoReservaDto> pagosAsociados,
            Boolean generarDevolucion,
            FormaPago formaPago
    ) {
        ReservaCancelacionRequestDto dto = new ReservaCancelacionRequestDto();
        dto.setGenerarDevolucion(generarDevolucion);
        dto.setFormaPago(formaPago);

        if (Boolean.TRUE.equals(generarDevolucion)) {
            dto.setImporteDevolucion(BigDecimal.valueOf(500));
        }

        return new CancelacionReservaValidationContext(
                dto,
                estadoReserva,
                pagosAsociados
        );
    }

    private PagoAsociadoReservaDto crearPago() {
        return new PagoAsociadoReservaDto(
                1L,
                LocalDate.of(2026, 7, 1),
                BigDecimal.valueOf(5000),
                FormaPago.EFECTIVO
        );
    }
}