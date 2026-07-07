package com.cipolflo.server.clientes.validator;

import org.springframework.stereotype.Component;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.utils.RutNormalizador;
@Component
public class RutFormatoValidator {
     private static final int[] FACTORES = {4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public void validar(String rut) {
        if (rut == null || rut.isBlank()) lanzarInvalido();

        String soloDigitos = RutNormalizador.normalizar(rut);

        if (soloDigitos.length() != 12) {
            lanzarInvalido();
        }

        int[] digitos = soloDigitos.chars().map(c -> c - '0').toArray();

        int suma = 0;
        for (int i = 0; i < FACTORES.length; i++) {
            suma += FACTORES[i] * digitos[i];
        }

        int resto = suma % 11;
        int verificadorEsperado = (11 - resto) % 11;

        if (digitos[11] != verificadorEsperado) {
            lanzarInvalido();
        }
    }

    private void lanzarInvalido() {
        throw new ClienteValidacionException(
                ClienteCodigoError.RUT_INVALIDO.name(),
                "El RUT ingresado no es válido"
        );
    }
}
