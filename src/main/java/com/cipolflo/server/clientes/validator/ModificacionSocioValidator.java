package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.ModificacionSocioRequestDto;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
        validarFechaIngreso(dto.getFechaIngreso());
        cedulaFormatoValidator.validar(dto.getCedula());
        cedulaUnicaValidator.validar(cedulaNormalizada, id);
        emailFormatoValidator.validar(dto.getMail());
        emailUnicoValidator.validar(mailNormalizado, id);
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
