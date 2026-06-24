package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FabricaEstrategiaTest {

    private FabricaEstrategia fabrica;

    @BeforeEach
    void setUp() {
        fabrica = new FabricaEstrategia(
                new EstrategiaCostoPorDia(),
                new EstrategiaCostoPorHora(),
                new EstrategiaCostoPorUnidad(),
                new EstrategiaCostoPorPersona(),
                new EstrategiaCostoPorDiaPorPersona()
        );
    }

    @Test
    void porDia_retornaEstrategiaCorrecta() {
        assertThat(fabrica.obtener(ModalidadPrecio.POR_DIA)).isInstanceOf(EstrategiaCostoPorDia.class);
    }

    @Test
    void porHora_retornaEstrategiaCorrecta() {
        assertThat(fabrica.obtener(ModalidadPrecio.POR_HORA)).isInstanceOf(EstrategiaCostoPorHora.class);
    }

    @Test
    void porUnidad_retornaEstrategiaCorrecta() {
        assertThat(fabrica.obtener(ModalidadPrecio.POR_UNIDAD)).isInstanceOf(EstrategiaCostoPorUnidad.class);
    }

    @Test
    void porPersona_retornaEstrategiaCorrecta() {
        assertThat(fabrica.obtener(ModalidadPrecio.POR_PERSONA)).isInstanceOf(EstrategiaCostoPorPersona.class);
    }

    @Test
    void porDiaPorPersona_retornaEstrategiaCorrecta() {
        assertThat(fabrica.obtener(ModalidadPrecio.POR_DIA_POR_PERSONA)).isInstanceOf(EstrategiaCostoPorDiaPorPersona.class);
    }

    @Test
    void todasLasModalidadesTienenEstrategia() {
        for (ModalidadPrecio modalidad : ModalidadPrecio.values()) {
            assertThat(fabrica.obtener(modalidad))
                    .as("Falta estrategia para modalidad: " + modalidad)
                    .isNotNull();
        }
    }
}
