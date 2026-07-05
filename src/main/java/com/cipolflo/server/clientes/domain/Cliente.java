package com.cipolflo.server.clientes.domain;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class Cliente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String cedula;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String telefono;

    private String mail;

    private String notas;

    public void modificar(String nombreCompleto, String telefono, String mail, String notas) {
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.mail = mail;
        this.notas = notas;
    }
}
