package com.cipolflo.server.servicios.domain;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.AuditableEntity;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "servicio")
@Getter
@Setter
@NoArgsConstructor
public class Servicio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Procedencia procedencia;

    @Column(nullable = false)
    private BigDecimal precioParticular;

    @Column(nullable = false)
    private BigDecimal precioSocio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadPrecio modalidadPrecio;

    private Integer capacidad;

    private Integer cantidad;

    @Column(nullable = false)
    private Boolean habilitado = true;

    public void cambiarHabilitacion() {
        this.habilitado = !this.habilitado;
    }

    public void modificar(String nombre, BigDecimal precioParticular, BigDecimal precioSocio,
                          ModalidadPrecio modalidadPrecio, Integer capacidad, Integer cantidad) {
        if (precioSocio.compareTo(precioParticular) >= 0) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.PRECIO_SOCIO_MAYOR_O_IGUAL_PARTICULAR.name(),
                    "El precio socio debe ser menor al precio particular"
            );
        }
        this.nombre = nombre;
        this.precioParticular = precioParticular;
        this.precioSocio = precioSocio;
        this.modalidadPrecio = modalidadPrecio;
        this.capacidad = capacidad;
        this.cantidad = cantidad;
    }

    public static Servicio registrar(
            String nombre,
            Procedencia procedencia,
            BigDecimal precioParticular,
            BigDecimal precioSocio,
            ModalidadPrecio modalidadPrecio,
            Integer capacidad,
            Integer cantidad) {

        if (precioSocio.compareTo(precioParticular) >= 0) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.PRECIO_SOCIO_MAYOR_O_IGUAL_PARTICULAR.name(),
                    "El precio socio debe ser menor al precio particular"
            );
        }

        Servicio servicio = new Servicio();
        servicio.nombre = nombre;
        servicio.procedencia = procedencia;
        servicio.precioParticular = precioParticular;
        servicio.precioSocio = precioSocio;
        servicio.modalidadPrecio = modalidadPrecio;
        servicio.capacidad = capacidad;
        servicio.cantidad = cantidad;
        servicio.habilitado = true;

        return servicio;
    }
}
