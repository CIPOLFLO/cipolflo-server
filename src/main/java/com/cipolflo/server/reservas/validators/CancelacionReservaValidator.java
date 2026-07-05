package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.contexto.CancelacionReservaValidationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CancelacionReservaValidator {

    public void validar(CancelacionReservaValidationContext context) {
        validarReservaCancelable(context.getReserva());
        validarImporteDevolucion(context);

        if (!context.getPagosAsociados().isEmpty()) {
            validarDecisionDevolucion(context.getDto().getGenerarDevolucion());
            validarFormaPagoParaDevolucion(context.getDto());
        }
    }

    private void validarReservaCancelable(Reserva reserva) {
        if (!reserva.esCancelable()) {
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

    private void validarImporteDevolucion(CancelacionReservaValidationContext context) {
        ReservaCancelacionRequestDto dto = context.getDto();

        if (!Boolean.TRUE.equals(dto.getGenerarDevolucion())) {
            return;
        }

        if (dto.getImporteDevolucion() == null
                || dto.getImporteDevolucion().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.IMPORTE_DEVOLUCION_INVALIDO,
                    "El importe de devolución debe ser mayor a cero"
            );
        }

        if (dto.getImporteDevolucion().compareTo(context.getImporteTotalPagos()) > 0) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.IMPORTE_DEVOLUCION_SUPERA_TOTAL_PAGADO,
                    "El importe de devolución no puede superar el total pagado"
            );
        }
    }
}
