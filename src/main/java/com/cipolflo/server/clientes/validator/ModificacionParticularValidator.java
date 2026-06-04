package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.ModificacionParticularRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ModificacionParticularValidator {

    private final CedulaFormatoValidator cedulaFormatoValidator;
    private final CedulaUnicaValidator cedulaUnicaValidator;
    private final EmailUnicoValidator emailUnicoValidator;

    public ModificacionParticularValidator(CedulaFormatoValidator cedulaFormatoValidator,
                                           CedulaUnicaValidator cedulaUnicaValidator,
                                           EmailUnicoValidator emailUnicoValidator) {
        this.cedulaFormatoValidator = cedulaFormatoValidator;
        this.cedulaUnicaValidator = cedulaUnicaValidator;
        this.emailUnicoValidator = emailUnicoValidator;
    }

    public void validar(Long id, ModificacionParticularRequestDto dto) {
        cedulaFormatoValidator.validar(dto.getCedula());
        cedulaUnicaValidator.validar(dto.getCedula(), id);
        emailUnicoValidator.validar(dto.getMail(), id);
    }
}
