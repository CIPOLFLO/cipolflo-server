package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.dto.CalculoCostoRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import org.springframework.stereotype.Component;

@Component
public class CalculoCostoValidator {

    public void validar(CalculoCostoRequestDto dto, ModalidadPrecio modalidad) {
        validarFechas(dto);
        if (modalidad == ModalidadPrecio.POR_HORA) {
            validarHoras(dto);
        }
    }

    private void validarFechas(CalculoCostoRequestDto dto) {
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.FECHA_FIN_ANTERIOR_A_INICIO,
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
    }

    private void validarHoras(CalculoCostoRequestDto dto) {
        if (dto.getHoraInicio() == null || dto.getHoraFin() == null) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.HORAS_REQUERIDAS_PARA_MODALIDAD_POR_HORA,
                    "Las horas de inicio y fin son obligatorias para servicios con modalidad POR_HORA"
            );
        }
    }
}
