package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import org.springframework.stereotype.Component;

@Component
public class EmailUnicoValidator {

    private final ClienteRepository clienteRepository;

    public EmailUnicoValidator(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public void validar(String mail, Long idExcluir) {
        if (mail == null || mail.isBlank()) return;

        if (clienteRepository.existsByMailIgnoreCaseAndIdNot(mail.trim(), idExcluir)) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.EMAIL_DUPLICADO.name(),
                    "El email ingresado ya está en uso"
            );
        }
    }
}
