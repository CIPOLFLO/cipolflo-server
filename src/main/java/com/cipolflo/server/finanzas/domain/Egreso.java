package com.cipolflo.server.finanzas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("EGRESO")
@Getter
@Setter
@NoArgsConstructor
public class Egreso extends Finanza {
}
