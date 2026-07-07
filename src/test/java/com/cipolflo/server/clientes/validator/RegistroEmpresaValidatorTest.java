package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.dto.RegistroEmpresaRequestDto;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RegistroEmpresaValidatorTest {

    @Mock
    private RutFormatoValidator rutFormatoValidator;

    @Mock
    private RutUnicaValidator rutUnicaValidator;

    @Mock
    private EmailFormatoValidator emailFormatoValidator;

    @Mock
    private EmailUnicoValidator emailUnicoValidator;

    @InjectMocks
    private RegistroEmpresaValidator validator;

    private static final String RUT_NORMALIZADO = "211003420017";
    private static final String MAIL_NORMALIZADO = "empresa@mail.com";

    @Test
    void deberiaValidarRegistroEmpresaCorrectamente() {
        RegistroEmpresaRequestDto dto = crearDto();

        validator.validar(dto, RUT_NORMALIZADO, MAIL_NORMALIZADO);

        verify(rutFormatoValidator).validar(dto.getRut());
        verify(rutUnicaValidator).validar(RUT_NORMALIZADO);
        verify(emailFormatoValidator).validar(dto.getMail());
        verify(emailUnicoValidator).validar(MAIL_NORMALIZADO);
    }

    @Test
    void deberiaPropagarErrorCuandoRutFormatoEsInvalido() {
        RegistroEmpresaRequestDto dto = crearDto();

        doThrow(new ClienteValidacionException("RUT_INVALIDO", "El RUT ingresado no es válido"))
                .when(rutFormatoValidator).validar(dto.getRut());

        assertThrows(ClienteValidacionException.class,
                () -> validator.validar(dto, RUT_NORMALIZADO, MAIL_NORMALIZADO));

        verify(rutFormatoValidator).validar(dto.getRut());
        verifyNoInteractions(rutUnicaValidator, emailFormatoValidator, emailUnicoValidator);
    }

    @Test
    void deberiaPropagarErrorCuandoRutYaExiste() {
        RegistroEmpresaRequestDto dto = crearDto();

        doThrow(new ClienteValidacionException("RUT_DUPLICADO", "Ya existe un cliente con ese RUT"))
                .when(rutUnicaValidator).validar(RUT_NORMALIZADO);

        assertThrows(ClienteValidacionException.class,
                () -> validator.validar(dto, RUT_NORMALIZADO, MAIL_NORMALIZADO));

        verify(rutFormatoValidator).validar(dto.getRut());
        verify(rutUnicaValidator).validar(RUT_NORMALIZADO);
        verifyNoInteractions(emailFormatoValidator, emailUnicoValidator);
    }

    @Test
    void deberiaPropagarErrorCuandoEmailYaExiste() {
        RegistroEmpresaRequestDto dto = crearDto();

        doThrow(new ClienteValidacionException("EMAIL_DUPLICADO", "Ya existe un cliente con ese email"))
                .when(emailUnicoValidator).validar(MAIL_NORMALIZADO);

        assertThrows(ClienteValidacionException.class,
                () -> validator.validar(dto, RUT_NORMALIZADO, MAIL_NORMALIZADO));

        verify(rutFormatoValidator).validar(dto.getRut());
        verify(rutUnicaValidator).validar(RUT_NORMALIZADO);
        verify(emailFormatoValidator).validar(dto.getMail());
        verify(emailUnicoValidator).validar(MAIL_NORMALIZADO);
    }

    private RegistroEmpresaRequestDto crearDto() {
        RegistroEmpresaRequestDto dto = new RegistroEmpresaRequestDto();
        dto.setRazonSocial("Antel S.A.");
        dto.setRut("21.100342.001-7");
        dto.setPais("Uruguay");
        dto.setDepartamento("Montevideo");
        dto.setCiudad("Montevideo");
        dto.setDireccion("Guatemala 1075");
        dto.setTelefono("099123456");
        dto.setMail("empresa@mail.com");
        dto.setObservaciones("Sin observaciones");
        return dto;
    }
}