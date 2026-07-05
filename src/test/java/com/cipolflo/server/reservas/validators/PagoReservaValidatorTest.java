package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.RegistroPagoReservaRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.contexto.PagoReservaValidationContext;
import com.cipolflo.server.shared.enums.FormaPago;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PagoReservaValidatorTest {

    private final PagoReservaValidator validator = new PagoReservaValidator();

    @Test
    void noDeberiaLanzarExcepcionCuandoPagoEsValido() {
        PagoReservaValidationContext context = crearContext(
                EstadoReserva.PENDIENTE,
                TipoReserva.COMUN,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(1500)
        );

        assertDoesNotThrow(() -> validator.validar(context));
    }

    @Test
    void deberiaLanzarErrorCuandoReservaEstaFinalizada() {
        PagoReservaValidationContext context = crearContext(
                EstadoReserva.FINALIZADA,
                TipoReserva.COMUN,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(1500)
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.RESERVA_ESTADO_INVALIDO_PARA_PAGO.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoReservaEstaCancelada() {
        PagoReservaValidationContext context = crearContext(
                EstadoReserva.CANCELADA,
                TipoReserva.COMUN,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(1500)
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.RESERVA_ESTADO_INVALIDO_PARA_PAGO.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoReservaEsColaboracion() {
        PagoReservaValidationContext context = crearContext(
                EstadoReserva.PENDIENTE,
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(1500)
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.PAGO_NO_APLICA_COLABORACION.name(), exception.getCodigo());
    }

    @Test
    void deberiaLanzarErrorCuandoImporteSuperaSaldo() {
        PagoReservaValidationContext context = crearContext(
                EstadoReserva.PENDIENTE,
                TipoReserva.COMUN,
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(1500)
        );

        ReservaValidacionException exception = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(context)
        );

        assertEquals(ReservaCodigoError.PAGO_IMPORTE_SUPERA_SALDO.name(), exception.getCodigo());
    }

    private PagoReservaValidationContext crearContext(
            EstadoReserva estadoReserva,
            TipoReserva tipoReserva,
            BigDecimal importe,
            BigDecimal montoImpago
    ) {
        RegistroPagoReservaRequestDto dto = new RegistroPagoReservaRequestDto();
        dto.setImporte(importe);
        dto.setEsPagoTotal(false);
        dto.setFormaPago(FormaPago.EFECTIVO);

        return new PagoReservaValidationContext(
                dto,
                estadoReserva,
                tipoReserva,
                montoImpago
        );
    }
}