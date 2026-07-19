package com.cipolflo.server.integraciones.mensajeria.ai.tools;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.servicios.dto.ServicioReferenciaDto;
import com.cipolflo.server.servicios.dto.ServicioReservaOcupacionDto;
import com.cipolflo.server.servicios.service.IConsultaServicioSimple;
import com.cipolflo.server.servicios.service.IServicioService;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadToolsTest {

    @Mock
    private IConsultaServicioSimple consultaServicioSimple;

    @Mock
    private IServicioService servicioService;

    @InjectMocks
    private DisponibilidadTools disponibilidadTools;

    private static final LocalDate DESDE = LocalDate.of(2026, 3, 10);
    private static final LocalDate HASTA = LocalDate.of(2026, 3, 15);

    @Test
    void deberiaPedirCorreccionCuandoDesdeEsPosteriorAHasta() {
        String resultado = disponibilidadTools.consultarDisponibilidad("cabaña 2", null, HASTA, DESDE);

        assertTrue(resultado.toLowerCase().contains("fecha"));
        verify(consultaServicioSimple, never()).buscarPorNombre(any(), any());
    }

    @Test
    void deberiaAvisarCuandoFaltanFechas() {
        String resultado = disponibilidadTools.consultarDisponibilidad("cabaña 2", null, null, HASTA);

        assertTrue(resultado.toLowerCase().contains("fecha"));
        verify(consultaServicioSimple, never()).buscarPorNombre(any(), any());
    }

    @Test
    void deberiaAvisarCuandoElServicioNoExiste() {
        when(consultaServicioSimple.buscarPorNombre("inexistente", null)).thenReturn(List.of());

        String resultado = disponibilidadTools.consultarDisponibilidad("inexistente", null, DESDE, HASTA);

        assertTrue(resultado.toLowerCase().contains("no encontré"));
        verify(servicioService, never()).getFechasOcupadas(any(), any(), any());
    }

    @Test
    void dosServiciosConMismoNombreSinProcedencia_deberiaResponderPorAmbos() {
        ServicioReferenciaDto sede = new ServicioReferenciaDto(1L, "Cabaña 1", Procedencia.SEDE, true);
        ServicioReferenciaDto camping = new ServicioReferenciaDto(2L, "Cabaña 1", Procedencia.CAMPING, true);
        when(consultaServicioSimple.buscarPorNombre("cabaña 1", null)).thenReturn(List.of(sede, camping));
        when(servicioService.getFechasOcupadas(1L, DESDE, HASTA)).thenReturn(List.of());
        when(servicioService.getFechasOcupadas(2L, DESDE, HASTA)).thenReturn(List.of());

        String resultado = disponibilidadTools.consultarDisponibilidad("cabaña 1", null, DESDE, HASTA);

        assertTrue(resultado.contains("SEDE") || resultado.contains("Sede"));
        assertTrue(resultado.contains("CAMPING") || resultado.contains("Camping"));
        verify(servicioService).getFechasOcupadas(1L, DESDE, HASTA);
        verify(servicioService).getFechasOcupadas(2L, DESDE, HASTA);
    }

    @Test
    void conProcedenciaIndicada_deberiaResponderSoloPorEsa() {
        ServicioReferenciaDto camping = new ServicioReferenciaDto(2L, "Cabaña 1", Procedencia.CAMPING, true);
        when(consultaServicioSimple.buscarPorNombre("cabaña 1", Procedencia.CAMPING)).thenReturn(List.of(camping));
        when(servicioService.getFechasOcupadas(2L, DESDE, HASTA)).thenReturn(List.of());

        String resultado = disponibilidadTools.consultarDisponibilidad("cabaña 1", Procedencia.CAMPING, DESDE, HASTA);

        assertTrue(resultado.contains("CAMPING") || resultado.contains("Camping"));
        verify(servicioService, never()).getFechasOcupadas(eq(1L), any(), any());
    }

    @Test
    void deberiaInformarFechasOcupadasCuandoHayReservas() {
        ServicioReferenciaDto servicio = new ServicioReferenciaDto(1L, "Salón", Procedencia.SEDE, true);
        when(consultaServicioSimple.buscarPorNombre("salón", null)).thenReturn(List.of(servicio));
        when(servicioService.getFechasOcupadas(1L, DESDE, HASTA)).thenReturn(List.of(
                new ServicioReservaOcupacionDto(100L, EstadoReserva.CONFIRMADA,
                        LocalDate.of(2026, 3, 11), LocalDate.of(2026, 3, 12))));

        String resultado = disponibilidadTools.consultarDisponibilidad("salón", null, DESDE, HASTA);

        assertTrue(resultado.contains("2026-03-11"));
        assertTrue(resultado.contains("2026-03-12"));
    }

    @Test
    void deberiaInformarDisponibleCuandoNoHayOcupacion() {
        ServicioReferenciaDto servicio = new ServicioReferenciaDto(1L, "Salón", Procedencia.SEDE, true);
        when(consultaServicioSimple.buscarPorNombre("salón", null)).thenReturn(List.of(servicio));
        when(servicioService.getFechasOcupadas(1L, DESDE, HASTA)).thenReturn(List.of());

        String resultado = disponibilidadTools.consultarDisponibilidad("salón", null, DESDE, HASTA);

        assertTrue(resultado.toLowerCase().contains("disponible"));
    }

    @Test
    void deberiaAvisarCuandoElServicioEstaDeshabilitado() {
        ServicioReferenciaDto deshabilitado = new ServicioReferenciaDto(1L, "Cancha vieja", Procedencia.SEDE, false);
        when(consultaServicioSimple.buscarPorNombre("cancha vieja", null)).thenReturn(List.of(deshabilitado));

        String resultado = disponibilidadTools.consultarDisponibilidad("cancha vieja", null, DESDE, HASTA);

        assertTrue(resultado.toLowerCase().contains("deshabilitad"));
        verify(servicioService, never()).getFechasOcupadas(any(), any(), any());
    }

    @Test
    void masDeDiezCoincidencias_deberiaDevolverDiezYAvisarQueHayMas() {
        List<ServicioReferenciaDto> muchos = new ArrayList<>();
        for (long i = 1; i <= 12; i++) {
            muchos.add(new ServicioReferenciaDto(i, "Cabaña " + i, Procedencia.SEDE, true));
        }
        when(consultaServicioSimple.buscarPorNombre("cabaña", null)).thenReturn(muchos);
        when(servicioService.getFechasOcupadas(any(), eq(DESDE), eq(HASTA))).thenReturn(List.of());

        String resultado = disponibilidadTools.consultarDisponibilidad("cabaña", null, DESDE, HASTA);

        verify(servicioService, org.mockito.Mockito.times(10)).getFechasOcupadas(any(), eq(DESDE), eq(HASTA));
        assertTrue(resultado.toLowerCase().contains("más resultados") || resultado.toLowerCase().contains("hay más"));
    }
}
