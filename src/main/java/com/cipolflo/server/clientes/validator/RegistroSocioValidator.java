package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.RegistroSocioRequestDto;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
        validarFechaIngreso(dto.getFechaIngreso());
        cedulaFormatoValidator.validar(dto.getCedula());
        cedulaUnicaValidator.validar(cedulaNormalizada);
        emailFormatoValidator.validar(dto.getEmail());
        emailUnicoValidator.validar(mailNormalizado);
    }
    private void validarFechaIngreso(LocalDate fechaIngreso) {
        if (fechaIngreso != null && fechaIngreso.isAfter(LocalDate.now())) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.FECHA_INGRESO_INVALIDA.name(),
                    "La fecha de ingreso no puede ser posterior a la fecha actual"
            );
        }
    }
}