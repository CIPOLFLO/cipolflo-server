package com.cipolflo.server.shared.export;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class FormulaSanitizerTest {
     @Test
    void sanitizar_conPrefijoPeligroso_igual_agregaApostrofo() {
        assertThat(FormulaSanitizer.sanitizar("=SUMA(A1)")).isEqualTo("'=SUMA(A1)");
    }

    @Test
    void sanitizar_conPrefijoPeligroso_mas_agregaApostrofo() {
        assertThat(FormulaSanitizer.sanitizar("+1234")).isEqualTo("'+1234");
    }

    @Test
    void sanitizar_conPrefijoPeligroso_menos_agregaApostrofo() {
        assertThat(FormulaSanitizer.sanitizar("-1234")).isEqualTo("'-1234");
    }

    @Test
    void sanitizar_conPrefijoPeligroso_arroba_agregaApostrofo() {
        assertThat(FormulaSanitizer.sanitizar("@usuario")).isEqualTo("'@usuario");
    }

    @Test
    void sanitizar_conPrefijoPeligroso_tab_agregaApostrofo() {
        assertThat(FormulaSanitizer.sanitizar("\tvalor")).isEqualTo("'\tvalor");
    }

    @Test
    void sanitizar_conPrefijoPeligroso_retornoCarro_agregaApostrofo() {
        assertThat(FormulaSanitizer.sanitizar("\rvalor")).isEqualTo("'\rvalor");
    }

    @Test
    void sanitizar_conValorNormal_noModifica() {
        assertThat(FormulaSanitizer.sanitizar("Juan Pérez")).isEqualTo("Juan Pérez");
    }

    @Test
    void sanitizar_conNull_devuelveNull() {
        assertThat(FormulaSanitizer.sanitizar(null)).isNull();
    }

    @Test
    void sanitizar_conVacio_devuelveVacio() {
        assertThat(FormulaSanitizer.sanitizar("")).isEqualTo("");
    }
}
