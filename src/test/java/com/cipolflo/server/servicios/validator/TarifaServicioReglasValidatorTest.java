package com.cipolflo.server.servicios.validator;

import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TarifaServicioReglasValidatorTest {

    private final TarifaServicioReglasValidator validator =
            new TarifaServicioReglasValidator();

    @Test
    void deberiaAceptarTarifasValidas() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, null, null)
        );

        assertDoesNotThrow(() -> validator.validar(tarifas));
    }

    @Test
    void deberiaRechazarListaDeTarifasVacia() {
        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(List.of())
        );

        assertEquals(
                ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarCuandoFaltaTarifaParticular() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, null, null)
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarCuandoFaltaTarifaSocioComun() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null)
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarRangosSuperpuestosDelMismoTipoCliente() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 0, 5),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 5, 10)
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.TARIFA_SUPERPUESTA.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarRangosParcialmenteSuperpuestos() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 0, 10),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 8, 20)
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.TARIFA_SUPERPUESTA.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarCuandoUnaDeLasDosCarecenDeLimiteYSeSuperponen() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 0, 5),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 3, null)
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.TARIFA_SUPERPUESTA.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarDosTarifasDelMismoTipoSinNingunLimite() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_POLICIA, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_POLICIA, null, null)
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.TARIFA_SUPERPUESTA.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaAceptarRangosConsecutivosSinSuperposicion() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 0, 5),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 6, 10)
        );

        assertDoesNotThrow(() -> validator.validar(tarifas));
    }

    @Test
    void deberiaAceptarRangosAbiertosSinSuperposicion() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, null, 5),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, 6, null)
        );

        assertDoesNotThrow(() -> validator.validar(tarifas));
    }

    @Test
    void deberiaAceptarRangosDeDistintoTipoClienteAunqueSeSuperpongan() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_POLICIA, 0, 10),
                crearTarifa(TipoClienteTarifa.SOCIO_POLICIA_RETIRADO, 0, 10)
        );

        assertDoesNotThrow(() -> validator.validar(tarifas));
    }

    @Test
    void cumpleTarifasObligatoriasDebeSerFalseCuandoFaltaParticular() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, null, null)
        );

        assertFalse(validator.cumpleTarifasObligatorias(tarifas));
    }

    @Test
    void cumpleTarifasObligatoriasDebeSerTrueConAmbasObligatorias() {
        List<TarifaServicio> tarifas = List.of(
                crearTarifa(TipoClienteTarifa.PARTICULAR, null, null),
                crearTarifa(TipoClienteTarifa.SOCIO_COMUN, null, null)
        );

        assertTrue(validator.cumpleTarifasObligatorias(tarifas));
    }

    private TarifaServicio crearTarifa(
            TipoClienteTarifa tipoCliente,
            Integer antiguedadMinima,
            Integer antiguedadMaxima
    ) {
        return TarifaServicio.registrar(
                null,
                tipoCliente,
                BigDecimal.valueOf(1000),
                ModalidadPrecio.POR_DIA,
                antiguedadMinima,
                antiguedadMaxima
        );
    }
}
