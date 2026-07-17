package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.dto.ModificacionSocioRequestDto;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
    class ModificacionSocioValidatorTest {

        @Mock
        private CedulaFormatoValidator cedulaFormatoValidator;

        @Mock
        private CedulaUnicaValidator cedulaUnicaValidator;

        @Mock
        private EmailFormatoValidator emailFormatoValidator;

        @Mock
        private EmailUnicoValidator emailUnicoValidator;

        @InjectMocks
        private ModificacionSocioValidator validator;

        private static final Long ID = 1L;
        private static final String CEDULA_NORMALIZADA = "12345678";
        private static final String MAIL_NORMALIZADO = "juan@mail.com";

        @Test
        void deberiaValidarModificacionSocioCorrectamente() {
            ModificacionSocioRequestDto dto = crearDto();

            validator.validar(ID, dto, CEDULA_NORMALIZADA, MAIL_NORMALIZADO);

            verify(cedulaFormatoValidator).validar(dto.getCedula());
            verify(cedulaUnicaValidator).validar(CEDULA_NORMALIZADA, ID);
            verify(emailFormatoValidator).validar(dto.getMail());
            verify(emailUnicoValidator).validar(MAIL_NORMALIZADO, ID);
        }

        @Test
        void deberiaAceptarFechaIngresoIgualAHoy() {
            ModificacionSocioRequestDto dto = crearDto();
            dto.setFechaIngreso(LocalDate.now());

            assertDoesNotThrow(
                    () -> validator.validar(ID, dto, CEDULA_NORMALIZADA, MAIL_NORMALIZADO)
            );
        }

        @Test
        void deberiaAceptarFechaIngresoPasada() {
            ModificacionSocioRequestDto dto = crearDto();
            dto.setFechaIngreso(LocalDate.now().minusYears(5));

            assertDoesNotThrow(
                    () -> validator.validar(ID, dto, CEDULA_NORMALIZADA, MAIL_NORMALIZADO)
            );
        }

        @Test
        void deberiaRechazarFechaIngresoFutura() {
            ModificacionSocioRequestDto dto = crearDto();
            dto.setFechaIngreso(LocalDate.now().plusDays(1));

            ClienteValidacionException exception = assertThrows(
                    ClienteValidacionException.class,
                    () -> validator.validar(ID, dto, CEDULA_NORMALIZADA, MAIL_NORMALIZADO)
            );

            assertEquals(
                    ClienteCodigoError.FECHA_INGRESO_INVALIDA.name(),
                    exception.getCodigo()
            );

            verifyNoInteractions(
                    cedulaFormatoValidator,
                    cedulaUnicaValidator,
                    emailFormatoValidator,
                    emailUnicoValidator
            );
        }

        private ModificacionSocioRequestDto crearDto() {
            ModificacionSocioRequestDto dto = new ModificacionSocioRequestDto();
            dto.setCedula("1.234.567-8");
            dto.setNombreCompleto("Juan Pérez");
            dto.setTelefono("099123456");
            dto.setMail("juan@mail.com");
            dto.setNotas("Sin observaciones");
            dto.setFechaNacimiento(LocalDate.of(1990, Month.MAY, 10));
            dto.setPais("Uruguay");
            dto.setDepartamento("Montevideo");
            dto.setCiudad("Montevideo");
            dto.setDireccion("Av. Italia 1234");
            dto.setMetodoCobro(MetodoCobro.EFECTIVO);
            dto.setCategoriaSocio(CategoriaSocio.SOCIO_COMUN);
            dto.setFechaIngreso(LocalDate.of(2020, Month.JANUARY, 1));
            return dto;
        }
    }

