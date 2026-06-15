package com.cipolflo.server.clientes.dto;


import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.shared.enums.FormaPago;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroPagoCuotaRequestDto(
        Integer anioDesde,
        Integer mesDesde,
        Integer cantidadMeses,
        BigDecimal importeTotal,
        MetodoCobro metodoCobro,
        LocalDate fechaPago,
        String observaciones
) {


}
