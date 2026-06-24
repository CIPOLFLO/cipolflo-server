package com.cipolflo.server.finanzas.domain;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.shared.AuditableEntity;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "concepto_de_pago", nullable = false)
    private Concepto concepto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Procedencia procedencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_de_pago", nullable = false)
    private FormaPago formaPago;

    private String notas;

    public abstract TipoMovimiento getTipoMovimiento();

    protected void inicializar(
            LocalDate fecha,
            BigDecimal importe,
            Concepto concepto,
            FormaPago formaPago,
            Procedencia procedencia,
            String notas
    ) {
        this.fecha = fecha;
        this.importe = importe;
        this.concepto = concepto;
        this.formaPago = formaPago;
        this.procedencia = procedencia;
        this.notas = notas;
    }
}