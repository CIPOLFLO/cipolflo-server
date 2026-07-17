package com.cipolflo.server.shared.mantenimiento;

import com.cipolflo.server.finanzas.repository.FinanzaRepository;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import com.cipolflo.server.shared.scheduling.ClaveConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTareaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimpiezaReservasYFinanzasServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private FinanzaRepository finanzaRepository;

    @Mock
    private ConfiguracionTareaRepository configuracionTareaRepository;

    private LimpiezaReservasYFinanzasService servicio() {
        return new LimpiezaReservasYFinanzasService(reservaRepository, finanzaRepository, configuracionTareaRepository);
    }

    private void configurarRetencion(int aniosReservas, int aniosFinanzasSueltas) {
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.of(new ConfiguracionTarea(
                        ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS, String.valueOf(aniosReservas))));
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS))
                .thenReturn(Optional.of(new ConfiguracionTarea(
                        ClaveConfiguracionTarea.LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS, String.valueOf(aniosFinanzasSueltas))));
    }

    @Test
    void limpiarVencidos_conReservasYFinanzasSueltasVencidas_borraTodoYReportaCantidades() {
        configurarRetencion(2, 3);
        List<Long> idsReservas = List.of(10L, 11L);
        when(reservaRepository.findIdsByFechaSalidaLessThanEqual(any())).thenReturn(idsReservas);
        when(finanzaRepository.deleteIngresosByReservaIdIn(idsReservas)).thenReturn(2);
        when(finanzaRepository.deleteEgresosByReservaIdIn(idsReservas)).thenReturn(1);
        when(reservaRepository.deleteByIdIn(idsReservas)).thenReturn(2);
        when(finanzaRepository.deleteIngresosSueltosConFechaAnteriorA(any())).thenReturn(4);
        when(finanzaRepository.deleteEgresosSueltosConFechaAnteriorA(any())).thenReturn(0);

        String resumen = servicio().limpiarVencidos();

        ArgumentCaptor<LocalDate> limiteReservasCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reservaRepository).findIdsByFechaSalidaLessThanEqual(limiteReservasCaptor.capture());
        assertEquals(LocalDate.now(ZonaHoraria.URUGUAY).minusYears(2), limiteReservasCaptor.getValue());

        ArgumentCaptor<LocalDate> limiteFinanzasCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(finanzaRepository).deleteIngresosSueltosConFechaAnteriorA(limiteFinanzasCaptor.capture());
        assertEquals(LocalDate.now(ZonaHoraria.URUGUAY).minusYears(3), limiteFinanzasCaptor.getValue());

        assertTrue(resumen.contains("2 reservas"));
        assertTrue(resumen.contains("3 finanzas asociadas"));
        assertTrue(resumen.contains("4 finanzas sueltas"));
    }

    @Test
    void limpiarVencidos_sinReservasVencidas_noBorraFinanzasDeReservaYSoloLimpiaSueltas() {
        configurarRetencion(2, 3);
        when(reservaRepository.findIdsByFechaSalidaLessThanEqual(any())).thenReturn(List.of());
        when(finanzaRepository.deleteIngresosSueltosConFechaAnteriorA(any())).thenReturn(1);
        when(finanzaRepository.deleteEgresosSueltosConFechaAnteriorA(any())).thenReturn(0);

        String resumen = servicio().limpiarVencidos();

        verify(finanzaRepository, never()).deleteIngresosByReservaIdIn(any());
        verify(finanzaRepository, never()).deleteEgresosByReservaIdIn(any());
        verify(reservaRepository, never()).deleteByIdIn(any());
        assertTrue(resumen.contains("0 reservas"));
        assertTrue(resumen.contains("1 finanzas sueltas"));
    }

    @Test
    void limpiarVencidos_faltaConfiguracion_abortaSinBorrarNada() {
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> servicio().limpiarVencidos());
        verify(reservaRepository, never()).findIdsByFechaSalidaLessThanEqual(any());
    }

    @Test
    void limpiarVencidos_retencionNoPositiva_abortaSinBorrarNada() {
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.of(new ConfiguracionTarea(
                        ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS, "0")));

        assertThrows(IllegalArgumentException.class, () -> servicio().limpiarVencidos());
        verify(reservaRepository, never()).findIdsByFechaSalidaLessThanEqual(any());
    }
}
