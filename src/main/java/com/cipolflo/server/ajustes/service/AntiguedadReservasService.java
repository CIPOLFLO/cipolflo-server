package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.AntiguedadReservasRequestDto;
import com.cipolflo.server.ajustes.dto.AntiguedadReservasResponseDto;
import com.cipolflo.server.ajustes.mapper.AntiguedadReservasMapper;
import com.cipolflo.server.shared.scheduling.ClaveConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Expone {@code LIMPIEZA_RESERVAS_RETENCION_ANIOS} de {@code configuracion_tarea} —
 * la misma tabla y clave que ya lee {@code LimpiezaReservasYFinanzasService} en cada
 * corrida — para que se pueda editar desde Ajustes sin depender de un deploy.
 */
@Service
public class AntiguedadReservasService implements IAntiguedadReservasService {

    private final ConfiguracionTareaRepository configuracionTareaRepository;

    public AntiguedadReservasService(ConfiguracionTareaRepository configuracionTareaRepository) {
        this.configuracionTareaRepository = configuracionTareaRepository;
    }

    @Override
    public AntiguedadReservasResponseDto obtenerAntiguedad() {
        return AntiguedadReservasMapper.toResponseDto(buscarConfiguracion());
    }

    @Override
    @Transactional
    public AntiguedadReservasResponseDto actualizarAntiguedad(AntiguedadReservasRequestDto dto) {
        ConfiguracionTarea configuracion = buscarConfiguracion();
        configuracion.actualizarValorEntero(dto.anios());
        return AntiguedadReservasMapper.toResponseDto(configuracionTareaRepository.save(configuracion));
    }

    private ConfiguracionTarea buscarConfiguracion() {
        return configuracionTareaRepository.findById(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS)
                .orElseThrow(() -> new IllegalStateException(
                        "Falta configurar " + ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS
                                + " en configuracion_tarea."));
    }
}
