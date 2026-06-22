package com.cipolflo.server.shared.export;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class NombreArchivoExportTest {
     @Test
    void generar_conPrefijo_devuelveNombreConFormatoCorrecto() {
        String resultado = NombreArchivoExport.generar("clientes");

        assertThat(resultado)
                .matches("clientes_\\d{4}-\\d{2}-\\d{2}_\\d{4}\\.xlsx");
    }

    @Test
    void generar_conPrefijo_empiezaConElPrefijo() {
        String resultado = NombreArchivoExport.generar("clientes");

        assertThat(resultado).startsWith("clientes_");
    }

    @Test
    void generar_conPrefijo_terminaConExtension() {
        String resultado = NombreArchivoExport.generar("clientes");

        assertThat(resultado).endsWith(".xlsx");
    }
}
