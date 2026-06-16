package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.RegistroParticularRequestDto;
import org.springframework.stereotype.Component;

@Component
public class RegistroParticularValidator {

    private final CedulaFormatoValidator cedulaFormatoValidator;
    private final CedulaUnicaValidator cedulaUnicaValidator;

    public RegistroParticularValidator ( CedulaFormatoValidator cedulaFormatoValidator,
                                         CedulaUnicaValidator cedulaUnicaValidator
    ){
        this.cedulaFormatoValidator = cedulaFormatoValidator;
        this.cedulaUnicaValidator = cedulaUnicaValidator;
    }
    public void validar(
            RegistroParticularRequestDto dto,
            String cedulaNormalizada
    ) {
        cedulaFormatoValidator.validar(dto.getCedula());
        cedulaUnicaValidator.validar(cedulaNormalizada);
    }
}
