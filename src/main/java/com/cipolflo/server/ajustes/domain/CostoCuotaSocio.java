package com.cipolflo.server.ajustes.domain;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Valor de referencia del costo de la cuota social. Fila única (id fijo), sembrada por
 * migración: no tiene endpoint de alta ni de borrado, solo de lectura y actualización.
 * Es solo un valor de referencia para la pantalla de Ajustes — el importe de cada pago de
 * cuota sigue siendo libre (ver {@code RegistroPagoCuotaRequestDto.importeTotal}).
 */
@Entity
@Table(name = "costo_cuota_socio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CostoCuotaSocio extends AuditableEntity {

    public static final Long ID_FIJO = 1L;

    @Id
    private Long id;

    @Column(nullable = false)
    private BigDecimal monto;

    public CostoCuotaSocio(BigDecimal monto) {
        this.id = ID_FIJO;
        actualizarMonto(monto);
    }

    public void actualizarMonto(BigDecimal nuevoMonto) {
        if (nuevoMonto == null || nuevoMonto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la cuota social debe ser mayor a cero");
        }
        this.monto = nuevoMonto;
    }
}
