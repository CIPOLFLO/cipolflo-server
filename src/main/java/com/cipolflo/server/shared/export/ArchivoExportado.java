package com.cipolflo.server.shared.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArchivoExportado {

    private final String nombre;
    private final byte[] contenido;
}