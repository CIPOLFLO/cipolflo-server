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

    // Recibe el mail ya normalizado (trim) por el service antes de llamar al validador
    public void validar(String mail, Long idExcluir) {
        if (mail == null || mail.isBlank()) return;
        if (clienteRepository.existsByMailIgnoreCaseAndIdNot(mail, idExcluir)) {
            lanzarDuplicado();
        }
    }

    public void validar(String email) {
        if (email == null || email.isBlank()) return;
        if (clienteRepository.existsByMailIgnoreCase(email)) {
            lanzarDuplicado();
        }
    }

    private void lanzarDuplicado() {
        throw new ClienteValidacionException(
                ClienteCodigoError.EMAIL_DUPLICADO.name(),
                "Ya existe un cliente con ese email"
        );
    }
}
