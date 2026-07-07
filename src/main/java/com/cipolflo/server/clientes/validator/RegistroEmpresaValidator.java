package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.RegistroEmpresaRequestDto;
import org.springframework.stereotype.Component;

@Component
public class RegistroEmpresaValidator {

    private final RutFormatoValidator rutFormatoValidator;
    private final RutUnicaValidator rutUnicaValidator;
    private final EmailFormatoValidator emailFormatoValidator;
    private final EmailUnicoValidator emailUnicoValidator;

    public RegistroEmpresaValidator(
            RutFormatoValidator rutFormatoValidator,
            RutUnicaValidator rutUnicaValidator,
            EmailFormatoValidator emailFormatoValidator,
            EmailUnicoValidator emailUnicoValidator
    ) {
        this.rutFormatoValidator = rutFormatoValidator;
        this.rutUnicaValidator = rutUnicaValidator;
        this.emailFormatoValidator = emailFormatoValidator;
        this.emailUnicoValidator = emailUnicoValidator;
    }

    public void validar(RegistroEmpresaRequestDto dto, String rutNormalizado, String mailNormalizado) {
        rutFormatoValidator.validar(dto.getRut());
        rutUnicaValidator.validar(rutNormalizado);
        emailFormatoValidator.validar(dto.getMail());
        emailUnicoValidator.validar(mailNormalizado);
    }
}
