package com.cipolflo.server.servicios.dto;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.dto.ResponseDto;
import com.cipolflo.server.shared.enums.Procedencia;

import java.math.BigDecimal;

public class ServicioResponseDto implements ResponseDto {

        private Long id;
        private String nombre;
        private Procedencia procedencia;
        private Integer cantidad;
        private BigDecimal precioSocio;
        private BigDecimal precioParticular;
        private Integer capacidad;
        private Boolean habilitado;
        private ModalidadPrecio modalidadPrecio;

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

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public Procedencia getProcedencia() { return procedencia; }
        public Integer getCantidad() { return cantidad; }
        public BigDecimal getPrecioSocio() { return precioSocio; }
        public BigDecimal getPrecioParticular() { return precioParticular; }
        public Integer getCapacidad() { return capacidad; }
        public Boolean getHabilitado() { return habilitado; }
        public ModalidadPrecio getModalidadPrecio(){ return modalidadPrecio;}
    }


