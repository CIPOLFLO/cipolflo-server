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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaSociosAtrasadosTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private IPagoCuotaService pagoCuotaService;

    @InjectMocks
    private ConsultaSociosAtrasados consultaSociosAtrasados;

    private Socio socio(Long id, String cedula, EstadoSocio estado) {
        Socio socio = new Socio();
        socio.setId(id);
        socio.setNombreCompleto("Socio " + id);
        socio.setCedula(cedula);
        socio.setEstado(estado);
        return socio;
    }

    @SuppressWarnings("unchecked")
    private void mockSocios(List<Socio> socios) {
        when(clienteRepository.findAll(any(Specification.class))).thenReturn((List) socios);
    }

    @Test
    void buscarAtrasados_deberiaMapearLosSociosConAlMenosUnMesAdeudado() {
        Socio activo = socio(1L, "11111111", EstadoSocio.ACTIVO);
        Socio inactivo = socio(2L, "22222222", EstadoSocio.INACTIVO);
        mockSocios(List.of(activo, inactivo));

        when(pagoCuotaService.calcularMesesAdeudados(activo)).thenReturn(1);
        when(pagoCuotaService.calcularMesesAdeudados(inactivo)).thenReturn(4);

        List<SocioAtrasadoDto> resultado = consultaSociosAtrasados.buscarAtrasados();

        assertEquals(2, resultado.size());
        assertEquals(new SocioAtrasadoDto(1L, "Socio 1", "11111111", 1, EstadoSocio.ACTIVO), resultado.get(0));
        assertEquals(new SocioAtrasadoDto(2L, "Socio 2", "22222222", 4, EstadoSocio.INACTIVO), resultado.get(1));
    }

    @Test
    void buscarAtrasados_deberiaExcluirSociosAlDia() {
        Socio alDia = socio(1L, "11111111", EstadoSocio.ACTIVO);
        mockSocios(List.of(alDia));

        when(pagoCuotaService.calcularMesesAdeudados(alDia)).thenReturn(0);

        List<SocioAtrasadoDto> resultado = consultaSociosAtrasados.buscarAtrasados();

        assertEquals(0, resultado.size());
    }

    @Test
    void buscarAtrasados_sinCoincidencias_deberiaDevolverListaVacia() {
        mockSocios(List.of());

        List<SocioAtrasadoDto> resultado = consultaSociosAtrasados.buscarAtrasados();

        assertEquals(0, resultado.size());
    }
}
