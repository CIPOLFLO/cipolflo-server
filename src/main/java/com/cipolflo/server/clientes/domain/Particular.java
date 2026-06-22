package com.cipolflo.server.clientes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("PARTICULAR")
@Getter
@Setter
@NoArgsConstructor
public class Particular extends Cliente {

    public static Particular registrar(
            String cedula,
            String nombre,
            String celular,
            String mail,
            String notas
    ) {
        Particular particular = new Particular();
        particular.setCedula(cedula);
        particular.setNombreCompleto(nombre);
        particular.setTelefono(celular);
        particular.setMail(mail);
        particular.setNotas(notas);
        return particular;
    }

    public void modificar(String cedula, String nombreCompleto, String telefono, String mail, String notas) {
        this.setCedula(cedula);
        super.modificar(nombreCompleto, telefono, mail, notas);
    }

}
