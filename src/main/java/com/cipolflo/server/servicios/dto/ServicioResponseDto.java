package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.dto.AuditInfoDto;
import com.cipolflo.server.shared.dto.ResponseDto;
import com.cipolflo.server.shared.enums.Procedencia;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class ServicioResponseDto extends AuditInfoDto implements ResponseDto {

        private final Long id;
        private final String nombre;
        private final Procedencia procedencia;
        private final Integer cantidad;
        private final BigDecimal precioSocio;
        private final BigDecimal precioParticular;
        private final Integer capacidad;
        private final EstadoServicio estado;
        private final ModalidadPrecio modalidadPrecio;

        public ServicioResponseDto(
                Long id,
                String nombre,
                Procedencia procedencia,
                Integer cantidad,
                BigDecimal precioSocio,
                BigDecimal precioParticular,
                Integer capacidad,
                EstadoServicio estado,
                ModalidadPrecio modalidadPrecio,
                Instant createdAt,
                Instant updatedAt,
                String createdBy,
                String updatedBy) {

                super(createdAt, updatedAt, createdBy, updatedBy);
                this.id = id;
                this.nombre = nombre;
                this.procedencia = procedencia;
                this.cantidad = cantidad;
                this.precioSocio = precioSocio;
                this.precioParticular = precioParticular;
                this.capacidad = capacidad;
                this.estado = estado;
                this.modalidadPrecio = modalidadPrecio;
        }
}
