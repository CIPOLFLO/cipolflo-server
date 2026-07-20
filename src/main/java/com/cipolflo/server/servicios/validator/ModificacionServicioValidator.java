package com.cipolflo.server.servicios.validator;

import org.springframework.stereotype.Component;

@Component
public class ModificacionServicioValidator {

    private final NombreUnicoValidator nombreUnicoValidator;
    private final TarifaServicioReglasValidator tarifaServicioReglasValidator;

    public ModificacionServicioValidator(NombreUnicoValidator nombreUnicoValidator,  TarifaServicioReglasValidator tarifaServicioReglasValidator) {
        this.nombreUnicoValidator = nombreUnicoValidator;
        this.tarifaServicioReglasValidator = tarifaServicioReglasValidator;
    }

    public void validar(ModificacionValidationContext context) {
        nombreUnicoValidator.validar(context.getNombre(), context.getId());
        tarifaServicioReglasValidator.validar(context.getTarifas());
    }
}
