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

    public void modificar(String cedula, String nombreCompleto, String telefono, String mail, String notas) {
        this.setCedula(cedula);
        super.modificar(nombreCompleto, telefono, mail, notas);
    }
}
