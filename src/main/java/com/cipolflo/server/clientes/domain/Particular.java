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
}
