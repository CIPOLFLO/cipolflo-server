package com.cipolflo.server.reservas.domain;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.shared.AuditableEntity;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reserva extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private TipoReserva tipoReserva;

    @Column
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
    private LocalDate fechaEntrada;

    @Column(nullable = false)
    private LocalDate fechaSalida;

    private BigDecimal importe;

    private Integer cantidadTotal;

    private Integer cantidadMenores;

    private Integer cantidad;

    private String rut;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Boolean pago = false;

    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.NONE)
    private FormaPago formaPago;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Boolean requiereDocumentacion = false;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Boolean tieneDocumentacion = false;

    private String notas;

    public static Reserva crear(TipoReserva tipoReserva, Long clienteId, Long servicioId, Procedencia procedencia,
                                LocalDate fechaEntrada, LocalDate fechaSalida, Integer cantidadTotal, Integer cantidadMenores,
                                Integer cantidad, String rut, String notas,
                                boolean requiereDocumentacionPrevia) {
        Reserva r = new Reserva();
        r.tipoReserva = tipoReserva;
        r.clienteId = clienteId;
        r.servicioId = servicioId;
        r.procedencia = procedencia;
        r.fechaEntrada = fechaEntrada;
        r.fechaSalida = fechaSalida;
        r.cantidadTotal = cantidadTotal;
        r.cantidadMenores = cantidadMenores;
        r.cantidad = cantidad;
        r.rut = rut;
        r.notas = notas;
        r.requiereDocumentacion = requiereDocumentacionPrevia;
        r.estado = resolverEstado(r.tipoReserva);
        if (tipoReserva == TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO) {
            r.importe = BigDecimal.ZERO;
        }
        return r;
    }

    public void confirmarPago(BigDecimal importe, FormaPago formaPago) {
        if (this.pago) {
            throw new IllegalStateException("La reserva ya tiene el pago confirmado");
        }
        if (importe == null) {
            throw new IllegalArgumentException("El importe no puede ser nulo");
        }
        if (formaPago == null) {
            throw new IllegalArgumentException("La forma de pago no puede ser nula");
        }
        if (importe.signum() <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero");
        }
        this.importe = importe;
        this.formaPago = formaPago;
        this.pago = true;
        if (this.estado == EstadoReserva.PENDIENTE && this.tieneDocumentacion) {
            cambiarEstado(EstadoReserva.CONFIRMADA);
        }
    }

    public void recibirDocumentacion() {
        this.tieneDocumentacion = true;
        if (this.estado == EstadoReserva.PENDIENTE && this.pago) {
            cambiarEstado(EstadoReserva.CONFIRMADA);
        }
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
            case PENDIENTE   -> nuevoEstado == EstadoReserva.CONFIRMADA || nuevoEstado == EstadoReserva.CANCELADA;
            case CONFIRMADA  -> nuevoEstado == EstadoReserva.EN_CURSO || nuevoEstado == EstadoReserva.CANCELADA;
            case EN_CURSO    -> nuevoEstado == EstadoReserva.FINALIZADA;
            case FINALIZADA,CANCELADA  -> false;
        };
    }

    public void cancelar() {
        cambiarEstado(EstadoReserva.CANCELADA);
    }

    private static EstadoReserva resolverEstado(TipoReserva tipoReserva) {
        if(tipoReserva.equals(TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO)){
            return EstadoReserva.CONFIRMADA;
        }else{
            return EstadoReserva.PENDIENTE;
        }
    }
}
