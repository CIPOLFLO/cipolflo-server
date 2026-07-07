package com.cipolflo.server.clientes.validator;

import org.springframework.stereotype.Component;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
@Component
public class RutUnicaValidator {
     private final ClienteRepository clienteRepository;

    public RutUnicaValidator(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public void validar(String rut) {
        if (rut == null || rut.isBlank()) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.RUT_INVALIDO.name(),
                    "El RUT ingresado no es válido"
            );
        }

        if (clienteRepository.existsByRut(rut)) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.RUT_DUPLICADO.name(),
                    "Ya existe un cliente con ese RUT"
            );
        }
    }
}
