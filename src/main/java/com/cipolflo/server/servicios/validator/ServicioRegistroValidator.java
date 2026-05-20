package com.cipolflo.server.servicios.validator;

import org.springframework.stereotype.Component;
import com.cipolflo.server.servicios.dto.ServicioRegistroRequestDto;

@Component
public class ServicioRegistroValidator {

    private final NombreUnicoValidator nombreUnicoValidator;

    public ServicioRegistroValidator(NombreUnicoValidator nombreUnicoValidator) {
        this.nombreUnicoValidator = nombreUnicoValidator;
    }

    public void validar(ServicioRegistroRequestDto dto) {
        nombreUnicoValidator.validar(dto.getNombre(), null);
    }
}
