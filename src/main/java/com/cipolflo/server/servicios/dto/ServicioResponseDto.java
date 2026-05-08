package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.dto.ResponseDto;
import com.cipolflo.server.shared.enums.Procedencia;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ServicioResponseDto implements ResponseDto {

        private final Long id;
        private final String nombre;
        private final Procedencia procedencia;
        private final Integer cantidad;
        private final BigDecimal precioSocio;
        private final BigDecimal precioParticular;
        private final Integer capacidad;
        private final Boolean habilitado;
        private final ModalidadPrecio modalidadPrecio;

        public ServicioResponseDto(
                Long id,
                String nombre,
                Procedencia procedencia,
                Integer cantidad,
                BigDecimal precioSocio,
                BigDecimal precioParticular,
                Integer capacidad,
                Boolean habilitado,
                ModalidadPrecio modalidadPrecio) {

                this.id = id;
                this.nombre = nombre;
                this.procedencia = procedencia;
                this.cantidad = cantidad;
                this.precioSocio = precioSocio;
                this.precioParticular = precioParticular;
                this.capacidad = capacidad;
                this.habilitado = habilitado;
                this.modalidadPrecio = modalidadPrecio;

        }

}


