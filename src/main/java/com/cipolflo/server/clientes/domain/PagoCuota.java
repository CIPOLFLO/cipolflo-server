package com.cipolflo.server.clientes.domain;

import com.cipolflo.server.shared.enums.FormaPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pago_cuota")
@Getter
@Setter
@NoArgsConstructor
public class PagoCuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long socioId;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPago formaPago;

    @Column(nullable = false)
    private Integer cantidadMeses;
}
