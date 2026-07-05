package com.cipolflo.server.clientes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("EMPRESA")
@Getter
@Setter
@NoArgsConstructor
public class Empresa extends Cliente {

    @Column(unique = true, nullable = false)
    private String rut;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false)
    private String departamento;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private String direccion;

    public static Empresa registrar(String rut, String razonSocial, String telefono, String mail,
                                    String pais, String departamento, String ciudad, String direccion,
                                    String notas) {
        Empresa empresa = new Empresa();
        empresa.setRut(rut);
        empresa.setNombreCompleto(razonSocial);
        empresa.setTelefono(telefono);
        empresa.setMail(mail);
        empresa.setPais(pais);
        empresa.setDepartamento(departamento);
        empresa.setCiudad(ciudad);
        empresa.setDireccion(direccion);
        empresa.setNotas(notas);
        return empresa;
    }

    public void modificar(String rut, String razonSocial, String telefono, String mail, String notas,
                          String pais, String departamento, String ciudad, String direccion) {
        this.setRut(rut);
        super.modificar(razonSocial, telefono, mail, notas);
        this.pais = pais;
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.direccion = direccion;
    }
}
