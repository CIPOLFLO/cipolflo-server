package com.cipolflo.server.reservas.validators.contexto;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.RegistroPagoReservaRequestDto;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PagoReservaValidationContext{

    private final RegistroPagoReservaRequestDto pagoReservaDto;
    private final EstadoReserva estadoReserva;
    private final TipoReserva tipoReserva;
    private final BigDecimal montoImpago;

    public PagoReservaValidationContext(
            RegistroPagoReservaRequestDto pagoReservaDto,
            EstadoReserva estadoReserva,
            TipoReserva tipoReserva,
            BigDecimal montoImpago
    ){
        this.pagoReservaDto = pagoReservaDto;
        this.estadoReserva = estadoReserva;
        this.tipoReserva = tipoReserva;
        this.montoImpago = montoImpago;
    }

}
