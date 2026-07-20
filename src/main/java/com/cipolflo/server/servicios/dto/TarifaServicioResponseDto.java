package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TarifaServicioResponseDto {

    private Long id;

    private TipoClienteTarifa tipoCliente;

    private BigDecimal precio;

    private ModalidadPrecio modalidadPrecio;

    private Integer antiguedadMinima;

    private Integer antiguedadMaxima;
}
