package com.cipolflo.server.servicios.validator;


import org.springframework.stereotype.Component;

import com.cipolflo.server.servicios.dto.ServicioRegistroRequestDto;
import com.cipolflo.server.servicios.exception.ServicioPreciosException;
// import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
@Component
public class ServicioRegistroValidator {
    private final NombreUnicoValidator nombreUnicoValidator;



    public ServicioRegistroValidator(NombreUnicoValidator nombreUnicoValidator) {
        this.nombreUnicoValidator = nombreUnicoValidator;
    }

    public void validar(ServicioRegistroRequestDto dto) {
        // Validar nombre único (sin ID porque es nuevo)
        nombreUnicoValidator.validar(dto.getNombre(), null);
        
       
        validarPrecios(dto);
    }
    
    private void validarPrecios(ServicioRegistroRequestDto dto)  {
        if (dto.getPrecioParticular().compareTo(dto.getPrecioSocio()) > 0) {
            throw new ServicioPreciosException(
                ServicioCodigoError.PRECIO_SOCIO_MAYOR_O_IGUAL_PARTICULAR.name() + ": Precio Socio = " + dto.getPrecioSocio() + ", Precio Particular = " + dto.getPrecioParticular()
            );
        }
    }
}
