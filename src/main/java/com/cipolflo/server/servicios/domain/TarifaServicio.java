package com.cipolflo.server.servicios.domain;

import com.cipolflo.server.shared.AuditableEntity;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tarifa_servicio")
@Getter
@NoArgsConstructor
public class TarifaServicio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false)
    private TipoClienteTarifa tipoCliente;

    @Column(nullable = false)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad_precio", nullable = false)
    private ModalidadPrecio modalidadPrecio;

    @Column(name = "antiguedad_minima")
    private Integer antiguedadMinima;

    @Column(name = "antiguedad_maxima")
    private Integer antiguedadMaxima;

    public static TarifaServicio registrar(
            Servicio servicio,
            TipoClienteTarifa tipoCliente,
            BigDecimal precio,
            ModalidadPrecio modalidadPrecio,
            Integer antiguedadMinima,
            Integer antiguedadMaxima
    ) {
        validarInvariantes(
                tipoCliente,
                precio,
                antiguedadMinima,
                antiguedadMaxima
        );

        TarifaServicio tarifa = new TarifaServicio();
        tarifa.servicio = servicio;
        tarifa.tipoCliente = tipoCliente;
        tarifa.precio = precio;
        tarifa.modalidadPrecio = modalidadPrecio;
        tarifa.antiguedadMinima = antiguedadMinima;
        tarifa.antiguedadMaxima = antiguedadMaxima;

        return tarifa;
    }

    public void modificar(
            TipoClienteTarifa tipoCliente,
            BigDecimal precio,
            ModalidadPrecio modalidadPrecio,
            Integer antiguedadMinima,
            Integer antiguedadMaxima
    ) {
        validarInvariantes(
                tipoCliente,
                precio,
                antiguedadMinima,
                antiguedadMaxima
        );

        this.tipoCliente = tipoCliente;
        this.precio = precio;
        this.modalidadPrecio = modalidadPrecio;
        this.antiguedadMinima = antiguedadMinima;
        this.antiguedadMaxima = antiguedadMaxima;
    }

    private static void validarInvariantes(
            TipoClienteTarifa tipoCliente,
            BigDecimal precio,
            Integer antiguedadMinima,
            Integer antiguedadMaxima
    ) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.SOLICITUD_INVALIDA.name(),
                    "El precio de la tarifa debe ser mayor a cero"
            );
        }

        if (antiguedadMinima != null && antiguedadMinima < 0) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.SOLICITUD_INVALIDA.name(),
                    "La antigüedad mínima no puede ser negativa"
            );
        }

        if (antiguedadMaxima != null && antiguedadMaxima < 0) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.SOLICITUD_INVALIDA.name(),
                    "La antigüedad máxima no puede ser negativa"
            );
        }

        if (antiguedadMinima != null
                && antiguedadMaxima != null
                && antiguedadMinima > antiguedadMaxima) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.RANGO_ANTIGUEDAD_INVALIDO.name(),
                    "La antigüedad mínima debe ser menor o igual a la máxima"
            );
        }

        if (tipoCliente == TipoClienteTarifa.PARTICULAR
                && (antiguedadMinima != null || antiguedadMaxima != null)) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.ANTIGUEDAD_NO_APLICABLE_A_PARTICULAR.name(),
                    "La antigüedad no aplica a tarifas de tipo Particular"
            );
        }
    }
}