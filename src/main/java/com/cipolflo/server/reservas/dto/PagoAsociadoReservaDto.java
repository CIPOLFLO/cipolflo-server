package com.cipolflo.server.reservas.dto;

import com.cipolflo.server.shared.enums.FormaPago;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoAsociadoReservaDto(
        Long id,
        LocalDate fecha,
        BigDecimal importe,
        FormaPago formaPago
) {}
