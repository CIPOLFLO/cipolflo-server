package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import org.springframework.stereotype.Component;

@Component
public class CedulaFormatoValidator {

    private static final int[] FACTORES = {2, 9, 8, 7, 6, 3, 4};

    public void validar(String cedula) {
        if (cedula == null || cedula.isBlank()) lanzarInvalida();

        String soloDigitos = CedulaNormalizador.normalizar(cedula);

        if (soloDigitos.length() < 7 || soloDigitos.length() > 8) {
            lanzarInvalida();
        }

        String padded = String.format("%8s", soloDigitos).replace(' ', '0');
        int[] digitos = padded.chars().map(c -> c - '0').toArray();

        int suma = 0;
        for (int i = 0; i < FACTORES.length; i++) {
            suma += FACTORES[i] * digitos[i];
        }

        int verificadorEsperado = (10 - (suma % 10)) % 10;

        if (digitos[7] != verificadorEsperado) {
            lanzarInvalida();
        }
    }

    private void lanzarInvalida() {
        throw new ClienteValidacionException(
                ClienteCodigoError.CEDULA_INVALIDA.name(),
                "La cédula ingresada no es válida"
        );
    }
}
