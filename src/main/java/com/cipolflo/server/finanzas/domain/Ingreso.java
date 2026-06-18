package com.cipolflo.server.finanzas.domain;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
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
@DiscriminatorValue("INGRESO")
@Getter
@Setter
@NoArgsConstructor
public class Ingreso extends Finanza {

    private Long reservaId;

    private Long pagoCuotaId;

    public static Ingreso crearManual(
            LocalDate fecha,
            BigDecimal importe,
            Concepto concepto,
            FormaPago formaPago,
            Procedencia procedencia,
            String notas
    ) {
        Ingreso ingreso = new Ingreso();
        ingreso.inicializar(fecha, importe, concepto, formaPago, procedencia, notas);
        ingreso.setReservaId(null);
        ingreso.setPagoCuotaId(null);
        return ingreso;
    }
}