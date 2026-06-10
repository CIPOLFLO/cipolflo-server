package com.cipolflo.server.clientes.dto;

import java.time.Instant;
import com.cipolflo.server.shared.enums.FormaPago;

public record UltimaCuotaPagaDto(
        String mesCorrespondiente,
        Instant fechaPago,
        FormaPago formaPago
) {}