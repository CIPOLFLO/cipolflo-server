package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import org.springframework.stereotype.Component;

@Component
public class CedulaUnicaValidator {

    private final ClienteRepository clienteRepository;

    public CedulaUnicaValidator(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // TODO DEV-74: reemplazar esta consulta directa al repositorio por el servicio de búsqueda
    // de clientes por cédula cuando esté implementado, para centralizar la lógica de búsqueda.
    public void validar(String cedula, Long idExcluir) {
        if (cedula == null || cedula.isBlank()) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.CEDULA_INVALIDA.name(),
                    "La cédula ingresada no es válida"
            );
        }

        String normalizada = cedula.replaceAll("\\D", "");

        if (normalizada.isEmpty()) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.CEDULA_INVALIDA.name(),
                    "La cédula ingresada no es válida"
            );
        }

        if (clienteRepository.existsByCedulaAndIdNot(normalizada, idExcluir)) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.CEDULA_DUPLICADA.name(),
                    "Ya existe un cliente con esa cédula"
            );
        }
    }
}
