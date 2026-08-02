package com.cipolflo.server.manuales.service;

import com.cipolflo.server.manuales.domain.CategoriaManual;
import com.cipolflo.server.manuales.domain.Manual;
import com.cipolflo.server.manuales.dto.ManualResponseDto;
import com.cipolflo.server.manuales.exception.ManualNoDisponibleException;
import com.cipolflo.server.manuales.exception.ManualNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManualServiceTest {

    /** Manuales que el ResourceLoader de prueba considera publicados. */
    private final Set<Manual> publicados = new HashSet<>();

    private ManualService manualService;

    @BeforeEach
    void setUp() {
        ResourceLoader resourceLoader = new ResourceLoader() {

            @Override
            public Resource getResource(String location) {
                boolean existe = publicados.stream()
                        .anyMatch(manual -> location.endsWith(manual.getRutaClasspath()));

                // ByteArrayResource siempre existe; DescriptiveResource nunca.
                return existe
                        ? new ByteArrayResource("%PDF-1.4".getBytes(StandardCharsets.UTF_8))
                        : new DescriptiveResource(location);
            }

            @Override
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }
        };

        manualService = new ManualService(resourceLoader);
    }

    @Test
    void listarManuales_sinCategoria_devuelveTodoElCatalogo() {
        List<ManualResponseDto> manuales = manualService.listarManuales(null);

        assertThat(manuales).hasSize(Manual.values().length);
    }

    @Test
    void listarManuales_conCategoria_devuelveSoloEsaCategoria() {
        List<ManualResponseDto> manuales = manualService.listarManuales(CategoriaManual.USUARIO);

        assertThat(manuales)
                .isNotEmpty()
                .allMatch(dto -> dto.categoria() == CategoriaManual.USUARIO);
    }

    @Test
    void listarManuales_marcaComoDisponibleSoloLosQueTienenPdf() {
        publicados.add(Manual.CLIENTES);

        List<ManualResponseDto> manuales = manualService.listarManuales(null);

        assertThat(manuales)
                .filteredOn(ManualResponseDto::disponible)
                .extracting(ManualResponseDto::clave)
                .containsExactly(Manual.CLIENTES.getClave());
    }

    @Test
    void listarManuales_exponeElTituloVisible() {
        List<ManualResponseDto> manuales = manualService.listarManuales(CategoriaManual.USUARIO);

        assertThat(manuales)
                .extracting(ManualResponseDto::titulo)
                .contains("Manual de Módulo Clientes");
    }

    @Test
    void descargarManual_conPdfPublicado_devuelveElContenido() {
        publicados.add(Manual.CLIENTES);

        ManualDescarga descarga = manualService.descargarManual("clientes");

        assertThat(descarga.manual()).isEqualTo(Manual.CLIENTES);
        assertThat(descarga.contenido().exists()).isTrue();
    }

    @Test
    void descargarManual_ignoraMayusculas() {
        publicados.add(Manual.CLIENTES);

        assertThat(manualService.descargarManual("CLIENTES").manual()).isEqualTo(Manual.CLIENTES);
    }

    @Test
    void descargarManual_conClaveInexistente_lanzaNoEncontrado() {
        assertThatThrownBy(() -> manualService.descargarManual("no-existe"))
                .isInstanceOf(ManualNoEncontradoException.class);
    }

    @Test
    void descargarManual_conRutaMaliciosa_lanzaNoEncontrado() {
        assertThatThrownBy(() -> manualService.descargarManual("../application.properties"))
                .isInstanceOf(ManualNoEncontradoException.class);
    }

    @Test
    void descargarManual_sinPdfPublicado_lanzaNoDisponible() {
        assertThatThrownBy(() -> manualService.descargarManual("clientes"))
                .isInstanceOf(ManualNoDisponibleException.class)
                .hasMessageContaining("Manual de Módulo Clientes");
    }
}
