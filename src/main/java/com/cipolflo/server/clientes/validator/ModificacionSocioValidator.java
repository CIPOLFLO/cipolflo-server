package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.ModificacionSocioRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ModificacionSocioValidator {

    private final CedulaFormatoValidator cedulaFormatoValidator;
    private final CedulaUnicaValidator cedulaUnicaValidator;
    private final EmailUnicoValidator emailUnicoValidator;

    public ModificacionSocioValidator(CedulaFormatoValidator cedulaFormatoValidator,
                                      CedulaUnicaValidator cedulaUnicaValidator,
                                      EmailUnicoValidator emailUnicoValidator) {
        this.cedulaFormatoValidator = cedulaFormatoValidator;
        this.cedulaUnicaValidator = cedulaUnicaValidator;
        this.emailUnicoValidator = emailUnicoValidator;
    }

    public void validar(Long id, ModificacionSocioRequestDto dto) {
        cedulaFormatoValidator.validar(dto.getCedula());
        cedulaUnicaValidator.validar(dto.getCedula(), id);
        emailUnicoValidator.validar(dto.getMail(), id);
    }
}
