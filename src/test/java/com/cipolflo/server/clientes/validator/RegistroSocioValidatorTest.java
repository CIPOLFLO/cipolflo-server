package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.dto.RegistroSocioRequestDto;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RegistroSocioValidatorTest {

    @Mock
    private CedulaFormatoValidator cedulaFormatoValidator;

    @Mock
    private CedulaUnicaValidator cedulaUnicaValidator;

    @Mock
    private EmailFormatoValidator emailFormatoValidator;

    @Mock
    private EmailUnicoValidator emailUnicoValidator;

    @InjectMocks
    private RegistroSocioValidator validator;

    private static final String CEDULA_NORMALIZADA = "12345678";
    private static final String MAIL_NORMALIZADO = "juan@mail.com";

    @Test
    void deberiaValidarRegistroSocioCorrectamente() {
        RegistroSocioRequestDto dto = crearDto();

        validator.validar(dto, CEDULA_NORMALIZADA, MAIL_NORMALIZADO);

        verify(cedulaFormatoValidator).validar(dto.getCedula());
        verify(cedulaUnicaValidator).validar(CEDULA_NORMALIZADA);
        verify(emailFormatoValidator).validar(dto.getEmail());
        verify(emailUnicoValidator).validar(MAIL_NORMALIZADO);
    }

    @Test
    void deberiaPropagarErrorCuandoCedulaFormatoEsInvalido() {
        RegistroSocioRequestDto dto = crearDto();

        doThrow(new ClienteValidacionException("SOLICITUD_INVALIDA", "La cédula ingresada no es válida"))
                .when(cedulaFormatoValidator).validar(dto.getCedula());

        assertThrows(ClienteValidacionException.class,
                () -> validator.validar(dto, CEDULA_NORMALIZADA, MAIL_NORMALIZADO));

        verify(cedulaFormatoValidator).validar(dto.getCedula());
        verifyNoInteractions(cedulaUnicaValidator, emailFormatoValidator, emailUnicoValidator);
    }

    @Test
    void deberiaPropagarErrorCuandoEmailYaExiste() {
        RegistroSocioRequestDto dto = crearDto();

        doThrow(new ClienteValidacionException("EMAIL_DUPLICADO", "Ya existe un cliente con ese email"))
                .when(emailUnicoValidator).validar(MAIL_NORMALIZADO);

        assertThrows(ClienteValidacionException.class,
                () -> validator.validar(dto, CEDULA_NORMALIZADA, MAIL_NORMALIZADO));

        verify(cedulaFormatoValidator).validar(dto.getCedula());
        verify(cedulaUnicaValidator).validar(CEDULA_NORMALIZADA);
        verify(emailFormatoValidator).validar(dto.getEmail());
        verify(emailUnicoValidator).validar(MAIL_NORMALIZADO);
    }

    private RegistroSocioRequestDto crearDto() {
        RegistroSocioRequestDto dto = new RegistroSocioRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombreCompleto("Juan Pérez");
        dto.setFechaNacimiento(LocalDate.of(1990, Month.MAY, 10));
        dto.setTelefono("099123456");
        dto.setEmail("juan@mail.com");
        dto.setMetodoCobro(MetodoCobro.EFECTIVO);
        dto.setPais("Uruguay");
        dto.setDepartamento("Montevideo");
        dto.setCiudad("Montevideo");
        dto.setDireccion("Av. Italia 1234");
        dto.setObservaciones("Sin observaciones");
        return dto;
    }
}
