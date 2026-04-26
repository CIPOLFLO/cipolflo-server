package com.cipolflo.server.servicios.domain;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
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
}
