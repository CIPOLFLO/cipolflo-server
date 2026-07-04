package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.contexto.CancelacionReservaValidationContext;
import org.springframework.stereotype.Component;

@Component
public class CancelacionReservaValidator {

    public CancelacionReservaValidator() {}

    public void validar(CancelacionReservaValidationContext context) {
        validarEstadoReserva(context.getEstadoReserva());

        if (!context.getPagosAsociados().isEmpty()) {
            validarDecisionDevolucion(context.getDto().getGenerarDevolucion());
            validarFormaPagoParaDevolucion(context.getDto());
        }
    }

    private void validarEstadoReserva(EstadoReserva estado) {
        if (estado.equals(EstadoReserva.FINALIZADA) || estado.equals(EstadoReserva.CANCELADA)) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.RESERVA_NO_CANCELABLE,
                    "No se puede cancelar una reserva en este estado"
            );
        }
    }

    private void validarDecisionDevolucion(Boolean generarDevolucion) {
        if (generarDevolucion == null) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.DECISION_DEVOLUCION_REQUERIDA,
                    "Debe indicar si se genera devolución para la reserva"
            );
        }
    }

    private void validarFormaPagoParaDevolucion(ReservaCancelacionRequestDto dto) {
        if (Boolean.TRUE.equals(dto.getGenerarDevolucion()) && dto.getFormaPago() == null) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.FORMA_PAGO_REQUERIDA_PARA_DEVOLUCION,
                    "Debe indicar la forma de pago para la devolución"
            );
        }
    }
}
