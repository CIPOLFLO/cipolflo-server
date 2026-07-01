package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.contexto.PagoReservaValidationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PagoReservaValidator {

    public PagoReservaValidator(){}

    public void validar(PagoReservaValidationContext pagoReservaValidationContext){
        validarEstadoReserva(pagoReservaValidationContext.getEstadoReserva());
        validarTipoReserva(pagoReservaValidationContext.getTipoReserva());
        validarImporteContraSaldo(
                pagoReservaValidationContext.getPagoReservaDto().getImporte(),
                pagoReservaValidationContext.getMontoImpago()
        );
    }

    private void validarEstadoReserva(EstadoReserva estado){
        if(estado.equals(EstadoReserva.FINALIZADA) ||
            estado.equals(EstadoReserva.CANCELADA)){
            throw new ReservaValidacionException(
                    ReservaCodigoError.RESERVA_ESTADO_INVALIDO_PARA_PAGO,
                    "No se puede pagar una reserva en este estado"
            );
        }
    }

    private void validarTipoReserva(TipoReserva tipo){
        if(!tipo.equals(TipoReserva.COMUN)){
            throw new ReservaValidacionException(
                    ReservaCodigoError.PAGO_NO_APLICA_COLABORACION,
                    "No se registran pagos para las reservas que son colaboraciones sin fines de lucro"
            );
        }
    }

    private void validarImporteContraSaldo(BigDecimal importe, BigDecimal montoImpago){
        if(importe.compareTo(montoImpago) > 0){
            throw new ReservaValidacionException(
                    ReservaCodigoError.PAGO_IMPORTE_SUPERA_SALDO,
                    "El importe ingresado es mayor al monto que falta pagar"
            );
        }
    }
}
