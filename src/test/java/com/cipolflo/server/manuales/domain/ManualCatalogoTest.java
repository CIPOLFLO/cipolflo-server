package com.cipolflo.server.manuales.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Protege la coherencia del catálogo de manuales.
 *
 * <p>El chequeo central es el de metadata: un PDF publicado sin versión declarada
 * (o al revés) rompe el build, así que no se puede subir un manual corregido y olvidarse
 * de actualizar la versión y la fecha que ve el usuario.</p>
 */
class ManualCatalogoTest {

    private static boolean pdfPublicado(Manual manual) {
        return new ClassPathResource(manual.getRutaClasspath()).exists();
    }

    @ParameterizedTest
    @EnumSource(Manual.class)
    void manualConPdfPublicado_declaraVersionYFecha(Manual manual) {
        if (!pdfPublicado(manual)) {
            return;
        }

        assertThat(manual.getVersion())
                .as("El manual '%s' tiene el PDF publicado: debe declarar version en Manual.java",
                        manual.getClave())
                .isNotBlank();

        assertThat(manual.getFechaActualizacion())
                .as("El manual '%s' tiene el PDF publicado: debe declarar fechaActualizacion en Manual.java",
                        manual.getClave())
                .isNotNull();
    }

    @ParameterizedTest
    @EnumSource(Manual.class)
    void manualConVersionDeclarada_tieneElPdfEnElClasspath(Manual manual) {
        if (!manual.tieneVersionDeclarada()) {
            return;
        }

        assertThat(pdfPublicado(manual))
                .as("El manual '%s' declara version pero falta el archivo %s",
                        manual.getClave(), manual.getRutaClasspath())
                .isTrue();
    }

    @ParameterizedTest
    @EnumSource(Manual.class)
    void fechaDeActualizacionNoEsFutura(Manual manual) {
        if (!manual.tieneVersionDeclarada()) {
            return;
        }

        assertThat(manual.getFechaActualizacion())
                .as("El manual '%s' declara una fecha de actualización futura", manual.getClave())
                .isBeforeOrEqualTo(LocalDate.now());
    }

    @Test
    void lasClavesSonUnicas() {
        List<String> claves = Arrays.stream(Manual.values())
                .map(Manual::getClave)
                .toList();

        assertThat(claves).doesNotHaveDuplicates();
    }

    @Test
    void losNombresDeArchivoSonUnicos() {
        List<String> archivos = Arrays.stream(Manual.values())
                .map(Manual::getNombreArchivo)
                .toList();

        assertThat(archivos).doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @EnumSource(Manual.class)
    void laClaveEsUsableEnUnaUrl(Manual manual) {
        assertThat(manual.getClave())
                .as("La clave de '%s' se usa tal cual en la URL", manual.name())
                .matches("[a-z0-9-]+");
    }

    @ParameterizedTest
    @EnumSource(Manual.class)
    void elArchivoEsUnPdfDeLaCarpetaDeSuCategoria(Manual manual) {
        assertThat(manual.getNombreArchivo()).endsWith(".pdf");
        assertThat(manual.getRutaClasspath())
                .startsWith("manuales/" + manual.getCategoria().getCarpeta() + "/");
    }

    @Test
    void porClave_ignoraMayusculasYEspacios() {
        assertThat(Manual.porClave("  CLIENTES ")).contains(Manual.CLIENTES);
    }

    @Test
    void porClave_conClaveInexistenteOVacia_devuelveVacio() {
        assertThat(Manual.porClave("no-existe")).isEmpty();
        assertThat(Manual.porClave("")).isEmpty();
        assertThat(Manual.porClave(null)).isEmpty();
    }

    @Test
    void porCategoria_filtraPorCategoria() {
        assertThat(Manual.porCategoria(CategoriaManual.TECNICO))
                .allMatch(manual -> manual.getCategoria() == CategoriaManual.TECNICO)
                .isNotEmpty();
    }

    @Test
    void porCategoria_sinCategoria_devuelveTodos() {
        assertThat(Manual.porCategoria(null)).hasSize(Manual.values().length);
    }

    @Test
    void nombreDeDescarga_usaElTituloVisible() {
        assertThat(Manual.CLIENTES.getNombreArchivoDescarga())
                .isEqualTo("Manual de Módulo Clientes.pdf");
    }

    @ParameterizedTest
    @EnumSource(Manual.class)
    void nombreDeDescarga_noTieneCaracteresInvalidosEnWindows(Manual manual) {
        assertThat(manual.getNombreArchivoDescarga())
                .as("El título de '%s' deriva el nombre del archivo descargado", manual.getClave())
                .endsWith(".pdf")
                .doesNotContain("\\", "/", ":", "*", "?", "\"", "<", ">", "|");
    }
}
