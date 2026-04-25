package com.cipolflo.server.reservas.domain;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.shared.AuditableEntity;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
public class Reserva extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Long clienteId;

    @Column(nullable = false)
    private Long servicioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private EstadoReserva estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Procedencia procedencia;

    @Column(nullable = false)
    private Instant fechaEntrada;

    @Column(nullable = false)
    private Instant fechaSalida;

    private BigDecimal importe;

    private Integer cantidadPersonas;

    private Integer cantidadMenores;

    @Column(nullable = false)
    private Boolean pago = false;

    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    @Column(nullable = false)
    private Boolean documentacion = false;

    @Setter
    private String notas;

    public void recibirDocumentacion() {
        this.documentacion = true;
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public void confirmarPago(BigDecimal importe, FormaPago formaPago) {
        if (this.pago) {
            throw new IllegalStateException("La reserva ya tiene el pago confirmado");
        }
        this.importe = importe;
        this.formaPago = formaPago;
        this.pago = true;
    }

    public void cambiarEstado(EstadoReserva nuevoEstado) {
        if (!esTransicionValida(nuevoEstado)) {
            throw new IllegalStateException(
                    "Transición inválida: " + this.estado + " → " + nuevoEstado
            );
        }
        this.estado = nuevoEstado;
    }

    private boolean esTransicionValida(EstadoReserva nuevoEstado) {
        return switch (this.estado) {
            case PENDIENTE   -> nuevoEstado == EstadoReserva.CONFIRMADA
                    || nuevoEstado == EstadoReserva.EN_CURSO;
            case CONFIRMADA  -> nuevoEstado == EstadoReserva.EN_CURSO;
            case EN_CURSO    -> nuevoEstado == EstadoReserva.FINALIZADA;
            case FINALIZADA  -> false;
        };
    }
}
