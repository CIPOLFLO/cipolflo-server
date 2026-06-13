package com.cipolflo.server.clientes.dto;


import com.cipolflo.server.shared.enums.FormaPago;

import java.math.BigDecimal;

public record RegistroPagoCuotaRequestDto(
        Integer anioDesde,
        Integer mesDesde,
        Integer cantidadMeses,
        BigDecimal importeTotal,
        FormaPago formaPago,
        String observaciones
) {


}
