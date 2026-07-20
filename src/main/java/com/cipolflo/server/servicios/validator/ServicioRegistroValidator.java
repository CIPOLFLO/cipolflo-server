package com.cipolflo.server.servicios.validator;

import org.springframework.stereotype.Component;
import com.cipolflo.server.servicios.dto.ServicioRegistroRequestDto;

@Component
public class ServicioRegistroValidator {

    private final NombreUnicoValidator nombreUnicoValidator;
    private final TarifaServicioReglasValidator tarifaServicioReglasValidator;


    public ServicioRegistroValidator(NombreUnicoValidator nombreUnicoValidator, TarifaServicioReglasValidator tarifaServicioReglasValidator) {
        this.nombreUnicoValidator = nombreUnicoValidator;
        this.tarifaServicioReglasValidator = tarifaServicioReglasValidator;
    }

    public void validar(ServicioRegistroRequestDto dto) {

        nombreUnicoValidator.validar(dto.getNombre(), null);
        tarifaServicioReglasValidator.validar(dto.getTarifas());
    }
}
