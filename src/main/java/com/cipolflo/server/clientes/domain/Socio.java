package com.cipolflo.server.clientes.domain;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import java.time.Period;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("SOCIO")
@Getter
@Setter
@NoArgsConstructor
public class Socio extends Cliente implements ClienteConUbicacion {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaSocio categoriaSocio;

    public int calcularAntiguedadEnAnios(LocalDate fechaReferencia) {
        return Period.between(this.fechaIngreso, fechaReferencia).getYears();
    }

    public void pasarAInactivoPorMorosidad() {
        this.estado = EstadoSocio.INACTIVO;
    }

    public void darDeBaja() {
        this.estado = EstadoSocio.DE_BAJA;
    }

    public static Socio registrar(String cedula, String nombreCompleto, String telefono, String mail, String notas,
                                  LocalDate fechaNacimiento, String pais, String departamento,
                                  String ciudad, String direccion, MetodoCobro metodoCobro,
                                  CategoriaSocio categoriaSocio, LocalDate fechaIngreso) {
        Socio socio = new Socio();
        socio.setCedula(cedula);
        socio.setNombreCompleto(nombreCompleto);
        socio.setTelefono(telefono);
        socio.setMail(mail);
        socio.setNotas(notas);
        socio.fechaNacimiento = fechaNacimiento;
        socio.pais = pais;
        socio.departamento = departamento;
        socio.ciudad = ciudad;
        socio.direccion = direccion;
        socio.metodoCobro = metodoCobro;
        socio.categoriaSocio = categoriaSocio;
        socio.fechaIngreso = fechaIngreso;
        return socio;
    }

    public void modificar(String cedula, String nombreCompleto, String telefono, String mail, String notas,
                          LocalDate fechaNacimiento, String pais, String departamento,
                          String ciudad, String direccion, MetodoCobro metodoCobro,  CategoriaSocio categoriaSocio,
                          LocalDate fechaIngreso) {
        this.setCedula(cedula);
        super.modificar(nombreCompleto, telefono, mail, notas);
        this.fechaNacimiento = fechaNacimiento;
        this.pais = pais;
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.metodoCobro = metodoCobro;
        this.categoriaSocio = categoriaSocio;
        this.fechaIngreso = fechaIngreso;
    }

}
