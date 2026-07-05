package com.cipolflo.server.clientes.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmpresaTest {

    @Test
    void registrar_deberiaSetearTodosLosCampos() {
        Empresa empresa = Empresa.registrar(
                "210001230018",
                "Cipolatti S.A.",
                "099777777",
                "empresa@mail.com",
                "Uruguay",
                "Montevideo",
                "Montevideo",
                "Av. Libertador 500",
                "Cliente frecuente"
        );

        assertEquals("210001230018", empresa.getRut());
        assertEquals("Cipolatti S.A.", empresa.getNombreCompleto()); // razonSocial → nombreCompleto
        assertEquals("099777777", empresa.getTelefono());
        assertEquals("empresa@mail.com", empresa.getMail());
        assertEquals("Uruguay", empresa.getPais());
        assertEquals("Montevideo", empresa.getDepartamento());
        assertEquals("Montevideo", empresa.getCiudad());
        assertEquals("Av. Libertador 500", empresa.getDireccion());
        assertEquals("Cliente frecuente", empresa.getNotas());
        assertNull(empresa.getCedula());
    }

    @Test
    void registrar_deberiaAceptarMailYNotasNulos() {
        Empresa empresa = Empresa.registrar(
                "210001230018", "Cipolatti S.A.", "099777777", null,
                "Uruguay", "Montevideo", "Montevideo", "Av. Libertador 500", null);

        assertNull(empresa.getMail());
        assertNull(empresa.getNotas());
    }

    @Test
    void modificar_deberiaActualizarTodosLosCampos() {
        Empresa empresa = Empresa.registrar(
                "210001230018", "Cipolatti S.A.", "099777777", "empresa@mail.com",
                "Uruguay", "Montevideo", "Montevideo", "Av. Libertador 500", "Nota vieja");

        empresa.modificar(
                "220002340029",
                "Cipolatti SRL",
                "099888888",
                "nuevo@mail.com",
                "Nota nueva",
                "Argentina",
                "Buenos Aires",
                "La Plata",
                "Calle Falsa 123"
        );

        assertEquals("220002340029", empresa.getRut());
        assertEquals("Cipolatti SRL", empresa.getNombreCompleto());
        assertEquals("099888888", empresa.getTelefono());
        assertEquals("nuevo@mail.com", empresa.getMail());
        assertEquals("Nota nueva", empresa.getNotas());
        assertEquals("Argentina", empresa.getPais());
        assertEquals("Buenos Aires", empresa.getDepartamento());
        assertEquals("La Plata", empresa.getCiudad());
        assertEquals("Calle Falsa 123", empresa.getDireccion());
    }
}
