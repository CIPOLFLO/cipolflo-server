package com.cipolflo.server.servicios.validator;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.dto.TarifaServicioRequestDto;
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
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        null
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        null,
                        null
                )
        );

        assertDoesNotThrow(() -> validator.validar(tarifas));
    }

    @Test
    void deberiaRechazarTarifasNulas() {
        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(null)
        );

        assertEquals(
                ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                exception.getCodigo()
        );
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
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        null,
                        null
                )
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
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        null
                )
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
    void deberiaRechazarTarifaParticularConAntiguedadMinima() {
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        1,
                        null
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        null,
                        null
                )
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.ANTIGUEDAD_NO_APLICABLE_A_PARTICULAR.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarTarifaParticularConAntiguedadMaxima() {
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        5
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        null,
                        null
                )
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.ANTIGUEDAD_NO_APLICABLE_A_PARTICULAR.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarAntiguedadMinimaNegativa() {
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        null
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        -1,
                        5
                )
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.RANGO_ANTIGUEDAD_INVALIDO.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarAntiguedadMaximaNegativa() {
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        null
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        null,
                        -1
                )
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.RANGO_ANTIGUEDAD_INVALIDO.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarCuandoAntiguedadMinimaEsMayorQueMaxima() {
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        null
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        10,
                        5
                )
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> validator.validar(tarifas)
        );

        assertEquals(
                ServicioCodigoError.RANGO_ANTIGUEDAD_INVALIDO.name(),
                exception.getCodigo()
        );
    }

    @Test
    void deberiaRechazarRangosSuperpuestosDelMismoTipoCliente() {
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        null
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        0,
                        5
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1200),
                        5,
                        10
                )
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
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        null
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        0,
                        5
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1200),
                        6,
                        10
                )
        );

        assertDoesNotThrow(() -> validator.validar(tarifas));
    }

    @Test
    void deberiaAceptarRangosAbiertosSinSuperposicion() {
        List<TarifaServicioRequestDto> tarifas = List.of(
                crearTarifa(
                        TipoClienteTarifa.PARTICULAR,
                        BigDecimal.valueOf(2500),
                        null,
                        null
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1500),
                        null,
                        5
                ),
                crearTarifa(
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.valueOf(1200),
                        6,
                        null
                )
        );

        assertDoesNotThrow(() -> validator.validar(tarifas));
    }

    private TarifaServicioRequestDto crearTarifa(
            TipoClienteTarifa tipoCliente,
            BigDecimal precio,
            Integer antiguedadMinima,
            Integer antiguedadMaxima
    ) {
        TarifaServicioRequestDto tarifa = new TarifaServicioRequestDto();

        tarifa.setTipoCliente(tipoCliente);
        tarifa.setPrecio(precio);
        tarifa.setModalidadPrecio(ModalidadPrecio.POR_DIA);
        tarifa.setAntiguedadMinima(antiguedadMinima);
        tarifa.setAntiguedadMaxima(antiguedadMaxima);

        return tarifa;
    }
}