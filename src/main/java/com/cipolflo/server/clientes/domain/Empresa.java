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
public class Empresa extends Cliente implements ClienteConUbicacion {

    // En herencia SINGLE_TABLE las columnas de subclase deben ser nullable a nivel de tabla
    // (las comparten Socio/Particular), por eso no se declara nullable = false.
    // La obligatoriedad de estos campos la garantizan las validaciones del RegistroEmpresaRequestDto.
    @Column(unique = true)
    private String rut;

    private String pais;

    private String departamento;

    private String ciudad;

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

    // Pendiente para la modificación de empresa (próximo ticket); aún sin endpoint/servicio que lo use.
    // public void modificar(String rut, String razonSocial, String telefono, String mail, String notas,
    //                       String pais, String departamento, String ciudad, String direccion) {
    //     this.setRut(rut);
    //     super.modificar(razonSocial, telefono, mail, notas);
    //     this.pais = pais;
    //     this.departamento = departamento;
    //     this.ciudad = ciudad;
    //     this.direccion = direccion;
    // }
}
