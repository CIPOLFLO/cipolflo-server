package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.ModificacionSocioRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ModificacionSocioValidator {

    private final CedulaFormatoValidator cedulaFormatoValidator;
    private final CedulaUnicaValidator cedulaUnicaValidator;
    private final EmailFormatoValidator emailFormatoValidator;
    private final EmailUnicoValidator emailUnicoValidator;

    public ModificacionSocioValidator(CedulaFormatoValidator cedulaFormatoValidator,
                                      CedulaUnicaValidator cedulaUnicaValidator,
                                      EmailFormatoValidator emailFormatoValidator,
                                      EmailUnicoValidator emailUnicoValidator) {
        this.cedulaFormatoValidator = cedulaFormatoValidator;
        this.cedulaUnicaValidator = cedulaUnicaValidator;
        this.emailFormatoValidator = emailFormatoValidator;
        this.emailUnicoValidator = emailUnicoValidator;
    }

    public void validar(Long id, ModificacionSocioRequestDto dto, String cedulaNormalizada, String mailNormalizado) {
        cedulaFormatoValidator.validar(dto.getCedula());
        cedulaUnicaValidator.validar(cedulaNormalizada, id);
        emailFormatoValidator.validar(dto.getMail());
        emailUnicoValidator.validar(mailNormalizado, id);
    }
}
