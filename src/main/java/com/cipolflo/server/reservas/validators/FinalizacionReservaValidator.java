package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.dto.ReservaFinalizacionRequestDto;
import org.springframework.stereotype.Component;

@Component
public class FinalizacionReservaValidator {

    public void validar(FinalizacionReservaValidationContext context) {
        validarEstadoFinalizable(context.getEstadoReserva());
        validarDecisionDePago(context);
        validarFormaPago(context);
    }

    private void validarEstadoFinalizable(EstadoReserva estadoReserva) {
        if (estadoReserva != EstadoReserva.EN_CURSO) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.RESERVA_NO_FINALIZABLE,
                    "Solo se pueden finalizar reservas en curso"
            );
        }
    }

    private void validarDecisionDePago(FinalizacionReservaValidationContext context) {
        if (context.isReservaPaga()) {
            return;
        }

        if (context.getDto().getCompletarPago() == null) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.DECISION_PAGO_REQUERIDA,
                    "Debe indicar si desea completar el pago pendiente"
            );
        }
    }

    private void validarFormaPago(FinalizacionReservaValidationContext context) {
        ReservaFinalizacionRequestDto dto = context.getDto();

        if (context.isReservaPaga()) {
            return;
        }

        if (Boolean.TRUE.equals(dto.getCompletarPago()) && dto.getFormaPago() == null) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.FORMA_PAGO_REQUERIDA_PARA_PAGO,
                    "Debe indicar la forma de pago para completar el saldo pendiente"
            );
        }
    }
}
