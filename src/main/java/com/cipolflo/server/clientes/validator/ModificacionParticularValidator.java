package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.ModificacionParticularRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ModificacionParticularValidator {

    private final CedulaFormatoValidator cedulaFormatoValidator;
    private final CedulaUnicaValidator cedulaUnicaValidator;
    private final EmailFormatoValidator emailFormatoValidator;
    private final EmailUnicoValidator emailUnicoValidator;

    public ModificacionParticularValidator(CedulaFormatoValidator cedulaFormatoValidator,
                                           CedulaUnicaValidator cedulaUnicaValidator,
                                           EmailFormatoValidator emailFormatoValidator,
                                           EmailUnicoValidator emailUnicoValidator) {
        this.cedulaFormatoValidator = cedulaFormatoValidator;
        this.cedulaUnicaValidator = cedulaUnicaValidator;
        this.emailFormatoValidator = emailFormatoValidator;
        this.emailUnicoValidator = emailUnicoValidator;
    }

    public void validar(Long id, ModificacionParticularRequestDto dto) {
        cedulaFormatoValidator.validar(dto.getCedula());
        cedulaUnicaValidator.validar(dto.getCedula(), id);
        emailFormatoValidator.validar(dto.getMail());
        emailUnicoValidator.validar(dto.getMail(), id);
    }
}
