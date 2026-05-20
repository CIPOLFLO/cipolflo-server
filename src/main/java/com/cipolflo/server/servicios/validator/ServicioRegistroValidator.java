package com.cipolflo.server.servicios.validator;


import org.springframework.stereotype.Component;

import com.cipolflo.server.servicios.dto.ServicioRegistroRequestDto;

// import com.cipolflo.server.servicios.exception.ServicioValidacionException;

@Component
public class ServicioRegistroValidator {
    private final NombreUnicoValidator nombreUnicoValidator;



    public ServicioRegistroValidator(NombreUnicoValidator nombreUnicoValidator) {
        this.nombreUnicoValidator = nombreUnicoValidator;
    }

    public void validar(ServicioRegistroRequestDto dto) {
        // Validar nombre único (sin ID porque es nuevo)
        nombreUnicoValidator.validar(dto.getNombre(), null);
        
       
       
    }
    
    
}
