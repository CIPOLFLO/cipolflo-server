package com.cipolflo.server.servicios.validator;

import com.cipolflo.server.shared.exception.ServicioCodigoError;
import com.cipolflo.server.servicios.exception.NombreDuplicadoException;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.springframework.stereotype.Component;

@Component
public class NombreUnicoValidator {

    private final ServicioRepository servicioRepository;

    public NombreUnicoValidator(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public void validar(String nombre, Long idExcluir) {

    boolean duplicado = idExcluir == null
            ? servicioRepository.existsByNombreIgnoreCase(nombre)
            : servicioRepository.existsByNombreIgnoreCaseAndIdNot(nombre, idExcluir);

    if (duplicado) {
        throw new NombreDuplicadoException(nombre)
        ;
    }
}
}
