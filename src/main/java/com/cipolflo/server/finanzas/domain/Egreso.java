package com.cipolflo.server.finanzas.domain;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("EGRESO")
@Getter
@Setter
@NoArgsConstructor
public class Egreso extends Finanza {

    private Long reservaId;

    public static Egreso crearManual(
            LocalDate fecha,
            BigDecimal importe,
            Concepto concepto,
            FormaPago formaPago,
            Procedencia procedencia,
            String notas
    ) {
        Egreso egreso = new Egreso();
        egreso.inicializar(fecha, importe, concepto, formaPago, procedencia, notas);
        egreso.setReservaId(null);
        return egreso;
    }

    public static Egreso crearDesdeCancelacionReserva(
            LocalDate fecha,
            BigDecimal importeTotal,
            FormaPago formaPago,
            Procedencia procedencia,
            String notas,
            Long reservaId
    ) {
        Egreso egreso = new Egreso();
        egreso.inicializar(
                fecha,
                importeTotal,
                Concepto.DEVOLUCION_RESERVA,
                formaPago,
                procedencia,
                notas
        );
        egreso.setReservaId(reservaId);
        return egreso;
    }
    @Override
    public TipoMovimiento getTipoMovimiento() {
        return TipoMovimiento.EGRESO;
    }
}