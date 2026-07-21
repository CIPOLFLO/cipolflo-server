package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.domain.CostoCuotaSocio;
import com.cipolflo.server.ajustes.dto.AntiguedadReservasRequestDto;
import com.cipolflo.server.ajustes.dto.CostoCuotaRequestDto;
import com.cipolflo.server.ajustes.dto.HabilitacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.repository.CostoCuotaSocioRepository;
import com.cipolflo.server.integraciones.telegram.domain.TelegramChatAutorizado;
import com.cipolflo.server.integraciones.telegram.repository.TelegramChatAutorizadoRepository;
import com.cipolflo.server.shared.scheduling.ClaveConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Verifica que las tres configuraciones de Ajustes son totalmente independientes: cada
 * service solo compone el repositorio dueño de su propia tabla, así que actualizar una no
 * puede tener efecto sobre las otras dos (ver notas técnicas del ticket de Ajustes).
 */
@ExtendWith(MockitoExtension.class)
class AjustesAislamientoTest {

    @Mock
    private CostoCuotaSocioRepository costoCuotaSocioRepository;

    @Mock
    private ConfiguracionTareaRepository configuracionTareaRepository;

    @Mock
    private TelegramChatAutorizadoRepository telegramChatAutorizadoRepository;

    private CostoCuotaService costoCuotaService;
    private AntiguedadReservasService antiguedadReservasService;
    private ClienteTelegramService clienteTelegramService;

    @BeforeEach
    void setUp() {
        costoCuotaService = new CostoCuotaService(costoCuotaSocioRepository);
        antiguedadReservasService = new AntiguedadReservasService(configuracionTareaRepository);
        clienteTelegramService = new ClienteTelegramService(telegramChatAutorizadoRepository);
    }

    @Test
    void actualizarCostoCuota_noTocaAntiguedadReservasNiClientesTelegram() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));
        when(costoCuotaSocioRepository.findById(CostoCuotaSocio.ID_FIJO)).thenReturn(Optional.of(costoCuota));
        when(costoCuotaSocioRepository.save(any(CostoCuotaSocio.class))).thenAnswer(inv -> inv.getArgument(0));

        costoCuotaService.actualizarCostoCuota(new CostoCuotaRequestDto(new BigDecimal("300.00")));

        verifyNoInteractions(configuracionTareaRepository);
        verifyNoInteractions(telegramChatAutorizadoRepository);
    }

    @Test
    void actualizarAntiguedad_noTocaCostoCuotaNiClientesTelegram() {
        ConfiguracionTarea configuracion = new ConfiguracionTarea(
                ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS, "2");
        when(configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS))
                .thenReturn(Optional.of(configuracion));
        when(configuracionTareaRepository.save(any(ConfiguracionTarea.class))).thenAnswer(inv -> inv.getArgument(0));

        antiguedadReservasService.actualizarAntiguedad(new AntiguedadReservasRequestDto(5));

        verifyNoInteractions(costoCuotaSocioRepository);
        verifyNoInteractions(telegramChatAutorizadoRepository);
    }

    @Test
    void cambiarHabilitacionClienteTelegram_noTocaCostoCuotaNiAntiguedadReservas() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(123L, "Juan", true);
        when(telegramChatAutorizadoRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(telegramChatAutorizadoRepository.save(any(TelegramChatAutorizado.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        clienteTelegramService.cambiarHabilitacion(1L, new HabilitacionClienteTelegramRequestDto(false));

        verifyNoInteractions(costoCuotaSocioRepository);
        verifyNoInteractions(configuracionTareaRepository);
    }
}
