package com.cipolflo.server.finanzas.domain;

import com.cipolflo.server.shared.enums.Procedencia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("INGRESO")
@Getter
@Setter
@NoArgsConstructor
public class Ingreso extends Finanza {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Procedencia procedencia;

    private Long reservaId;
}
