package com.cipolflo.server.clientes.domain;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "pago_cuota",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pago_cuota_socio_periodo",
                        columnNames = {"socio_id", "anio", "mes"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PagoCuota extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "socio_id", nullable = false)
    private Long socioId;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer mes;

    @Column(name = "fecha_pago", nullable = false)
    private Instant fechaPago;

    @Column(nullable = false)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_cobro", nullable = false)
    private MetodoCobro metodoCobro;

    private String observaciones;

    public static PagoCuota crear(
            Long socioId,
            Integer anio,
            Integer mes,
            Instant fechaPago,
            BigDecimal importe,
            MetodoCobro metodoCobro,
            String observaciones
    ) {
        PagoCuota pago = new PagoCuota();
        pago.setSocioId(socioId);
        pago.setAnio(anio);
        pago.setMes(mes);
        pago.setFechaPago(fechaPago);
        pago.setImporte(importe);
        pago.setMetodoCobro(metodoCobro);
        pago.setObservaciones(observaciones);
        return pago;
    }
}