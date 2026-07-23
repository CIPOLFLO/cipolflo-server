package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.AntiguedadReservasRequestDto;
import com.cipolflo.server.ajustes.dto.AntiguedadReservasResponseDto;
import com.cipolflo.server.shared.scheduling.ClaveConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTareaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AntiguedadReservasServiceTest {

    @Mock
    private ConfiguracionTareaRepository configuracionTareaRepository;

    @InjectMocks
    private AntiguedadReservasService antiguedadReservasService;

    private ConfiguracionTarea configuracion(String valor) {
        return new ConfiguracionTarea(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS, valor);
    }

    @Test
    void obtenerAntiguedad_devuelveElValorVigente() {
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.of(configuracion("2")));

        AntiguedadReservasResponseDto response = antiguedadReservasService.obtenerAntiguedad();

        assertEquals(2, response.anios());
    }

    @Test
    void obtenerAntiguedad_sinConfiguracionSembrada_lanzaIllegalStateException() {
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> antiguedadReservasService.obtenerAntiguedad());
    }

    @Test
    void actualizarAntiguedad_conAniosValido_actualizaYPersiste() {
        ConfiguracionTarea configuracion = configuracion("2");
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.of(configuracion));
        when(configuracionTareaRepository.save(any(ConfiguracionTarea.class))).thenAnswer(inv -> inv.getArgument(0));

        AntiguedadReservasResponseDto response =
                antiguedadReservasService.actualizarAntiguedad(new AntiguedadReservasRequestDto(5));

        assertEquals(5, response.anios());
        verify(configuracionTareaRepository).save(configuracion);
    }

    @Test
    void actualizarAntiguedad_conAniosInvalido_lanzaExcepcionYNoPersiste() {
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.of(configuracion("2")));

        assertThrows(IllegalArgumentException.class, () ->
                antiguedadReservasService.actualizarAntiguedad(new AntiguedadReservasRequestDto(0)));
    }

    @Test
    void actualizarAntiguedad_noConsultaLaClaveDeFinanzasSueltas() {
        ConfiguracionTarea configuracion = configuracion("2");
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.of(configuracion));
        when(configuracionTareaRepository.save(any(ConfiguracionTarea.class))).thenAnswer(inv -> inv.getArgument(0));

        antiguedadReservasService.actualizarAntiguedad(new AntiguedadReservasRequestDto(5));

        verify(configuracionTareaRepository, never())
                .findById(ClaveConfiguracionTarea.LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS);
        verify(configuracionTareaRepository, never())
                .save(argThat(c -> c.getClave() == ClaveConfiguracionTarea.LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS));
    }
}
