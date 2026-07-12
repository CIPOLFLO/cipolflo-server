package com.cipolflo.server.clientes.repository;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    private Socio socioConMesesSinPagar(String cedula, Integer mesesSinPagar) {
        Socio socio = new Socio();
        socio.setCedula(cedula);
        socio.setNombreCompleto("Socio " + cedula);
        socio.setTelefono("099000000");
        socio.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        socio.setEstado(EstadoSocio.ACTIVO);
        socio.setPais("Uruguay");
        socio.setDepartamento("Montevideo");
        socio.setCiudad("Montevideo");
        socio.setFechaIngreso(LocalDate.of(2020, 1, 1));
        socio.setMetodoCobro(MetodoCobro.EFECTIVO);
        socio.setMesesSinPagar(mesesSinPagar);
        return socio;
    }

    @Test
    void deberiaRetornarSoloSociosConMesesSinPagarMayorOIgualAlUmbral() {
        clienteRepository.saveAndFlush(socioConMesesSinPagar("1", 0));
        clienteRepository.saveAndFlush(socioConMesesSinPagar("2", 1));
        clienteRepository.saveAndFlush(socioConMesesSinPagar("3", 2));
        clienteRepository.saveAndFlush(socioConMesesSinPagar("4", 3));

        List<Socio> atrasados = clienteRepository.findByMesesSinPagarGreaterThanEqual(2);

        assertEquals(2, atrasados.size());
        assertTrue(atrasados.stream().allMatch(s -> s.getMesesSinPagar() >= 2));
    }
}
