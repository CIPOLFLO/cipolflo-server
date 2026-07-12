package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.dto.SocioAtrasadoDto;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaSociosAtrasadosTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ConsultaSociosAtrasados consultaSociosAtrasados;

    private Socio socio(Long id, String cedula, Integer mesesSinPagar, EstadoSocio estado) {
        Socio socio = new Socio();
        socio.setId(id);
        socio.setNombreCompleto("Socio " + id);
        socio.setCedula(cedula);
        socio.setMesesSinPagar(mesesSinPagar);
        socio.setEstado(estado);
        return socio;
    }

    @Test
    void buscarAtrasados_deberiaMapearLosSociosDevueltosPorElRepositorio() {
        when(clienteRepository.findByEstadoInAndMesesSinPagarGreaterThanEqual(
                eq(List.of(EstadoSocio.ACTIVO, EstadoSocio.INACTIVO)), eq(1)))
                .thenReturn(List.of(
                        socio(1L, "11111111", 1, EstadoSocio.ACTIVO),
                        socio(2L, "22222222", 4, EstadoSocio.INACTIVO)));

        List<SocioAtrasadoDto> resultado = consultaSociosAtrasados.buscarAtrasados();

        assertEquals(2, resultado.size());
        assertEquals(new SocioAtrasadoDto(1L, "Socio 1", "11111111", 1, EstadoSocio.ACTIVO), resultado.get(0));
        assertEquals(new SocioAtrasadoDto(2L, "Socio 2", "22222222", 4, EstadoSocio.INACTIVO), resultado.get(1));
        verify(clienteRepository).findByEstadoInAndMesesSinPagarGreaterThanEqual(
                List.of(EstadoSocio.ACTIVO, EstadoSocio.INACTIVO), 1);
    }

    @Test
    void buscarAtrasados_sinCoincidencias_deberiaDevolverListaVacia() {
        when(clienteRepository.findByEstadoInAndMesesSinPagarGreaterThanEqual(
                eq(List.of(EstadoSocio.ACTIVO, EstadoSocio.INACTIVO)), eq(1)))
                .thenReturn(List.of());

        List<SocioAtrasadoDto> resultado = consultaSociosAtrasados.buscarAtrasados();

        assertEquals(0, resultado.size());
    }
}
