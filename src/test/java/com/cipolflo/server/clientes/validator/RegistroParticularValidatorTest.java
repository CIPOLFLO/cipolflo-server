package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.RegistroParticularRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistroParticularValidatorTest {

    @Mock
    private CedulaFormatoValidator cedulaFormatoValidator;

    @Mock
    private CedulaUnicaValidator cedulaUnicaValidator;

    @InjectMocks
    private RegistroParticularValidator registroParticularValidator;

    @Test
    void deberiaValidarCedulaFormatoYCedulaUnica() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("1.234.567-8");

        registroParticularValidator.validar(dto, "12345678");

        verify(cedulaFormatoValidator).validar("1.234.567-8");
        verify(cedulaUnicaValidator).validar("12345678");
    }
}
