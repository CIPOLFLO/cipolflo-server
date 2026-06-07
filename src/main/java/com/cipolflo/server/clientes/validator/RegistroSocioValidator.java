package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.RegistroSocioRequestDto;
import org.springframework.stereotype.Component;

@Component
public class RegistroSocioValidator {

    private final CedulaFormatoValidator cedulaFormatoValidator;
    private final CedulaUnicaValidator cedulaUnicaValidator;
    private final EmailFormatoValidator emailFormatoValidator;
    private final EmailUnicoValidator emailUnicoValidator;

    public RegistroSocioValidator(
            CedulaFormatoValidator cedulaFormatoValidator,
            CedulaUnicaValidator cedulaUnicaValidator,
            EmailFormatoValidator emailFormatoValidator,
            EmailUnicoValidator emailUnicoValidator
    ) {
        this.cedulaFormatoValidator = cedulaFormatoValidator;
        this.cedulaUnicaValidator = cedulaUnicaValidator;
        this.emailFormatoValidator = emailFormatoValidator;
        this.emailUnicoValidator = emailUnicoValidator;
    }

    public void validar(RegistroSocioRequestDto dto, String cedulaNormalizada, String mailNormalizado) {
        cedulaFormatoValidator.validar(dto.getCedula());
        cedulaUnicaValidator.validar(cedulaNormalizada);
        emailFormatoValidator.validar(dto.getEmail());
        emailUnicoValidator.validar(mailNormalizado);
    }
}