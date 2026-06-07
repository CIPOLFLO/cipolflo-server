package com.cipolflo.server.clientes.domain;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("SOCIO")
@Getter
@Setter
@NoArgsConstructor
public class Socio extends Cliente {

    private Integer numeroSocio;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSocio estado;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false)
    private String departamento;

    @Column(nullable = false)
    private String ciudad;

    private String direccion;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    private LocalDate fechaUltimoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoCobro metodoCobro;

    @Column(nullable = false)
    private Integer mesesSinPagar = 0;

    public void incrementarMesesSinPagar() {
        this.mesesSinPagar++;
        if (this.mesesSinPagar >= 3) {
            this.estado = EstadoSocio.INACTIVO;
        }
    }

    public void darDeBaja() {
        this.estado = EstadoSocio.DE_BAJA;
    }

    public void modificar(String cedula, String nombreCompleto, String telefono, String mail, String notas,
                          LocalDate fechaNacimiento, String pais, String departamento,
                          String ciudad, String direccion, MetodoCobro metodoCobro) {
        this.setCedula(cedula);
        super.modificar(nombreCompleto, telefono, mail, notas);
        this.fechaNacimiento = fechaNacimiento;
        this.pais = pais;
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.metodoCobro = metodoCobro;
    }

}
