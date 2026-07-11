package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.ReservaFinalizacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.shared.enums.FormaPago;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinalizacionReservaValidatorTest {

    private final FinalizacionReservaValidator validator = new FinalizacionReservaValidator();

    @Test
    void noDeberiaLanzarExcepcionCuandoReservaEnCursoEstaPaga() {
        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();

        FinalizacionReservaValidationContext context = new FinalizacionReservaValidationContext(
                dto,
                EstadoReserva.EN_CURSO,
                true
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void noDeberiaLanzarExcepcionCuandoReservaEnCursoConSaldoCompletaPagoConFormaPago() {
        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();
        dto.setCompletarPago(true);
        dto.setFormaPago(FormaPago.EFECTIVO);

        FinalizacionReservaValidationContext context = new FinalizacionReservaValidationContext(
                dto,
                EstadoReserva.EN_CURSO,
                false
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void noDeberiaLanzarExcepcionCuandoReservaEnCursoConSaldoNoCompletaPago() {
        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();
        dto.setCompletarPago(false);

        FinalizacionReservaValidationContext context = new FinalizacionReservaValidationContext(
                dto,
                EstadoReserva.EN_CURSO,
                false
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void deberiaLanzarErrorCuandoReservaNoEstaEnCurso() {
        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();

        FinalizacionReservaValidationContext context = new FinalizacionReservaValidationContext(
                dto,
                EstadoReserva.CONFIRMADA,
                true
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.RESERVA_NO_FINALIZABLE.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoReservaConSaldoNoIndicaDecisionPago() {
        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();

        FinalizacionReservaValidationContext context = new FinalizacionReservaValidationContext(
                dto,
                EstadoReserva.EN_CURSO,
                false
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.DECISION_PAGO_REQUERIDA.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoCompletaPagoSinFormaPago() {
        ReservaFinalizacionRequestDto dto = new ReservaFinalizacionRequestDto();
        dto.setCompletarPago(true);

        FinalizacionReservaValidationContext context = new FinalizacionReservaValidationContext(
                dto,
                EstadoReserva.EN_CURSO,
                false
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.FORMA_PAGO_REQUERIDA_PARA_PAGO.name(), exception.getCodigo());
    }
}