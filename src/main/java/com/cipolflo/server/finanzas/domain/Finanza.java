package com.cipolflo.server.finanzas.domain;

import com.cipolflo.server.shared.AuditableEntity;
import com.cipolflo.server.shared.enums.FormaPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "finanza")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class Finanza extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private BigDecimal importe;

    @Column(nullable = false)
    private String conceptoDePago;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_de_pago", nullable = false)
    private FormaPago formaPago;

    private String notas;
}
