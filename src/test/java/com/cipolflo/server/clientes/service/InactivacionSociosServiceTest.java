package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.PagoCuota;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.PagoCuotaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InactivacionSociosServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PagoCuotaRepository pagoCuotaRepository;

    private InactivacionSociosService service() {
        return new InactivacionSociosService(clienteRepository, pagoCuotaRepository);
    }

    private Socio socio(Long id, LocalDate fechaIngreso) {
        Socio socio = new Socio();
        socio.setId(id);
        socio.setNombreCompleto("Socio " + id);
        socio.setCedula("1234567" + id);
        socio.setNumeroSocio(id.intValue());
        socio.setEstado(EstadoSocio.ACTIVO);
        socio.setFechaNacimiento(LocalDate.of(1990, Month.JANUARY, 1));
        socio.setPais("Uruguay");
        socio.setDepartamento("Montevideo");
        socio.setCiudad("Montevideo");
        socio.setFechaIngreso(fechaIngreso);
        socio.setMetodoCobro(MetodoCobro.EFECTIVO);
        return socio;
    }

    private PagoCuota pago(YearMonth periodo) {
        return PagoCuota.crear(1L, periodo.getYear(), periodo.getMonthValue(),
                Instant.now(), BigDecimal.valueOf(5000), MetodoCobro.EFECTIVO, null);
    }

    @SuppressWarnings("unchecked")
    private void mockSociosActivos(List<Socio> socios) {
        when(clienteRepository.findAll(any(Specification.class))).thenReturn((List) socios);
    }

    @Test
    void deberiaIgnorarSocioQuePagoElMesActual() {
        Socio socio = socio(1L, LocalDate.of(2025, Month.JANUARY, 1));
        mockSociosActivos(List.of(socio));

        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L))
                .thenReturn(Optional.of(pago(YearMonth.now())));

        String resumen = service().inactivarSociosMorosos();

        assertEquals(EstadoSocio.ACTIVO, socio.getEstado());
        assertTrue(resumen.contains("0 pasados a INACTIVO"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void noDeberiaInactivarCuandoAdeudaMenosDeTresMeses() {
        // El mes en curso no cuenta como adeudado (todavía no venció); pagó hace 2 meses
        // -> adeuda el mes anterior nomás (1 mes), no llega al umbral.
        Socio socio = socio(1L, LocalDate.of(2025, Month.JANUARY, 1));
        mockSociosActivos(List.of(socio));

        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L))
                .thenReturn(Optional.of(pago(YearMonth.now().minusMonths(2))));

        String resumen = service().inactivarSociosMorosos();

        assertEquals(EstadoSocio.ACTIVO, socio.getEstado());
        assertTrue(resumen.contains("0 pasados a INACTIVO"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deberiaPasarAInactivoAlAdeudarTresMesesCompletos() {
        // Pagó hace 4 meses -> adeuda los 3 meses completos anteriores al actual.
        Socio socio = socio(1L, LocalDate.of(2025, Month.JANUARY, 1));
        mockSociosActivos(List.of(socio));

        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L))
                .thenReturn(Optional.of(pago(YearMonth.now().minusMonths(4))));

        String resumen = service().inactivarSociosMorosos();

        assertEquals(EstadoSocio.INACTIVO, socio.getEstado());
        assertTrue(resumen.contains("1 pasados a INACTIVO"));
        verify(clienteRepository).save(socio);
    }

    @Test
    void deberiaUsarFechaIngresoCuandoElSocioNuncaPago() {
        Socio socio = socio(1L, LocalDate.now().minusMonths(4));
        mockSociosActivos(List.of(socio));

        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L))
                .thenReturn(Optional.empty());

        service().inactivarSociosMorosos();

        assertEquals(EstadoSocio.INACTIVO, socio.getEstado());
        verify(clienteRepository).save(socio);
    }

    @Test
    void deberiaIgnorarCuandoNoHaySociosActivos() {
        mockSociosActivos(List.of());

        String resumen = service().inactivarSociosMorosos();

        assertTrue(resumen.startsWith("0 socios activos evaluados"));
        verify(pagoCuotaRepository, never()).findTopBySocioIdOrderByAnioDescMesDesc(anyLong());
        verify(clienteRepository, never()).save(any());
    }
}
