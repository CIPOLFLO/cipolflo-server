package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailFormatoValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    public void validar(String mail) {
        if (mail == null || mail.isBlank()) return;

        if (!EMAIL_PATTERN.matcher(mail.trim()).matches()) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.EMAIL_INVALIDO.name(),
                    "El email ingresado no es válido"
            );
        }
    }
}
