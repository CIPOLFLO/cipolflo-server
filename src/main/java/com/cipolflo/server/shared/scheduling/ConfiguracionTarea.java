package com.cipolflo.server.shared.scheduling;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parámetro configurable de una tarea programada, guardado como par clave/valor para poder
 * ajustarlo sin necesidad de un deploy. Una fila por {@link ClaveConfiguracionTarea}, sembrada
 * por migración con su valor por defecto.
 */
@Entity
@Table(name = "configuracion_tarea")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfiguracionTarea extends AuditableEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 60)
    private ClaveConfiguracionTarea clave;

    @Setter
    @Column(nullable = false)
    private String valor;

    public ConfiguracionTarea(ClaveConfiguracionTarea clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public int valorComoEntero() {
        return Integer.parseInt(valor);
    }

    public void actualizarValorEntero(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException(clave + " debe ser > 0 (recibido: " + valor + ")");
        }
        this.valor = String.valueOf(valor);
    }
}
