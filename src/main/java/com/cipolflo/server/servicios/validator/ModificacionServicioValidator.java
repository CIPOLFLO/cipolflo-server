package com.cipolflo.server.servicios.validator;

import org.springframework.stereotype.Component;

@Component
public class ModificacionServicioValidator {

    private final NombreUnicoValidator nombreUnicoValidator;

    public ModificacionServicioValidator(NombreUnicoValidator nombreUnicoValidator) {
        this.nombreUnicoValidator = nombreUnicoValidator;
    }

    public void validar(ModificacionValidationContext context) {
        nombreUnicoValidator.validar(context.getNombre(), context.getId());
    }
}
