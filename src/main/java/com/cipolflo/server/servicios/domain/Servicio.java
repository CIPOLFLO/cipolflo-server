package com.cipolflo.server.servicios.domain;

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

    private Integer capacidad;

    private Integer cantidad;

    private BigDecimal costoPersonaExtra;

    @Column(nullable = false)
    private Boolean habilitado = true;

    public void cambiarHabilitacion() {
        this.habilitado = !this.habilitado;
    }

    public void modificar(String nombre, Integer capacidad, Integer cantidad,
                          BigDecimal costoPersonaExtra) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.cantidad = cantidad;
        this.costoPersonaExtra = costoPersonaExtra;
    }

    public static Servicio registrar(
            String nombre,
            Procedencia procedencia,
            Integer capacidad,
            Integer cantidad,
            BigDecimal costoPersonaExtra) {

        Servicio servicio = new Servicio();
        servicio.nombre = nombre;
        servicio.procedencia = procedencia;
        servicio.capacidad = capacidad;
        servicio.cantidad = cantidad;
        servicio.costoPersonaExtra = costoPersonaExtra;
        servicio.habilitado = true;
        return servicio;
    }
}
