package com.cipolflo.server.reservas.domain;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.shared.AuditableEntity;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private LocalDate fechaEntrada;

    @Column(nullable = false)
    private LocalDate fechaSalida;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private BigDecimal importe;

    private Integer cantidadTotal;

    private Integer cantidadMenores;

    private Integer cantidad;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Boolean pago = false;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private BigDecimal montoImpago;

    private LocalDateTime fechaLimitePago;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Boolean requiereDocumentacion = false;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Boolean tieneDocumentacion = false;

    private String notas;

    public static Reserva crear(TipoReserva tipoReserva, Long clienteId, Long servicioId, Procedencia procedencia,
                                LocalDate fechaEntrada, LocalDate fechaSalida, LocalTime horaInicio, LocalTime horaFin,
                                Integer cantidadTotal, Integer cantidadMenores,
                                Integer cantidad, String notas,
                                boolean requiereDocumentacionPrevia,
                                BigDecimal importe, LocalDateTime fechaLimite
    ) {
        Reserva r = new Reserva();
        r.tipoReserva = tipoReserva;
        r.clienteId = clienteId;
        r.servicioId = servicioId;
        r.procedencia = procedencia;
        r.fechaEntrada = fechaEntrada;
        r.fechaSalida = fechaSalida;
        r.horaInicio = horaInicio;
        r.horaFin = horaFin;
        r.cantidadTotal = cantidadTotal;
        r.cantidadMenores = cantidadMenores;
        r.cantidad = cantidad;
        r.notas = notas;
        r.requiereDocumentacion = requiereDocumentacionPrevia;
        r.estado = resolverEstado(tipoReserva);
        r.importe = resolverImporte(importe, tipoReserva);
        r.montoImpago = resolverImporte(importe, tipoReserva);
        r.fechaLimitePago = fechaLimite;
        return r;
    }

    public void registrarPago(BigDecimal importe, Boolean esPagoTotal) {
        if (this.pago) {
            throw new IllegalStateException("La reserva está paga");
        }
        if(esPagoTotal || importe.compareTo(this.getMontoImpago()) == 0){
            this.montoImpago = BigDecimal.ZERO;
            this.pago = true;
            if(esPagoTotal){
                this.setImporte(importe);
            }
        }else {
            this.montoImpago = this.montoImpago.subtract(importe);
            if (this.montoImpago.compareTo(BigDecimal.ZERO) == 0) {
                this.pago = true;
            }
        }
        if (this.getEstado().equals(EstadoReserva.PENDIENTE)
                && tienePagadoAlMenosLaMitad() && sePuedeConfirmar()) {
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

    public void modificar(Long servicioId, Procedencia procedencia, LocalDate fechaEntrada,
                          LocalDate fechaSalida, Integer cantidadTotal, Integer cantidadMenores,
                          Integer cantidad, String notas) {
        this.servicioId = servicioId;
        this.procedencia = procedencia;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.cantidadTotal = cantidadTotal;
        this.cantidadMenores = cantidadMenores;
        this.cantidad = cantidad;
        this.notas = notas;
    }

    public void cancelar() {
        cambiarEstado(EstadoReserva.CANCELADA);
    }

    // TODO: agregar método confirmar() cuando se implemente el ticket de confirmación manual de reserva

    private static EstadoReserva resolverEstado(TipoReserva tipoReserva) {
        if(tipoReserva.equals(TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO)){
            return EstadoReserva.CONFIRMADA;
        }else{
            return EstadoReserva.PENDIENTE;
        }
    }

    private static BigDecimal resolverImporte(BigDecimal importe, TipoReserva tipoReserva){
        BigDecimal imp = BigDecimal.ZERO;
        if (tipoReserva == TipoReserva.COMUN) {
            imp = importe;
        };
        return imp;
    }

    private Boolean sePuedeConfirmar()
    {
        return !this.requiereDocumentacion ||
                this.tieneDocumentacion;
    }

    private boolean tienePagadoAlMenosLaMitad() {
        BigDecimal montoPagado = getImporte().subtract(getMontoImpago());
        BigDecimal mitad = getImporte().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        return montoPagado.compareTo(mitad) >= 0;
    }
}