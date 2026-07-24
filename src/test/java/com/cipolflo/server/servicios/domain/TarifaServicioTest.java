package com.cipolflo.server.servicios.domain;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TarifaServicioTest {

    @Test
    void registrarDebeCrearUnaTarifaValidaParaSocioComun() {
        Servicio servicio = crearServicio();

        TarifaServicio tarifa = TarifaServicio.registrar(
                servicio,
                TipoClienteTarifa.SOCIO_COMUN,
                new BigDecimal("100"),
                ModalidadPrecio.POR_UNIDAD,
                0,
                5
        );

        assertSame(servicio, tarifa.getServicio());
        assertEquals(TipoClienteTarifa.SOCIO_COMUN, tarifa.getTipoCliente());
        assertEquals(new BigDecimal("100"), tarifa.getPrecio());
        assertEquals(ModalidadPrecio.POR_UNIDAD, tarifa.getModalidadPrecio());
        assertEquals(0, tarifa.getAntiguedadMinima());
        assertEquals(5, tarifa.getAntiguedadMaxima());
    }

    @Test
    void registrarDebeCrearUnaTarifaValidaParaParticularSinAntiguedad() {
        Servicio servicio = crearServicio();

        TarifaServicio tarifa = TarifaServicio.registrar(
                servicio,
                TipoClienteTarifa.PARTICULAR,
                new BigDecimal("150"),
                ModalidadPrecio.POR_UNIDAD,
                null,
                null
        );

        assertEquals(TipoClienteTarifa.PARTICULAR, tarifa.getTipoCliente());
        assertNull(tarifa.getAntiguedadMinima());
        assertNull(tarifa.getAntiguedadMaxima());
    }

    @Test
    void registrarDebeRechazarPrecioIgualACero() {
        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> TarifaServicio.registrar(
                        crearServicio(),
                        TipoClienteTarifa.SOCIO_COMUN,
                        BigDecimal.ZERO,
                        ModalidadPrecio.POR_UNIDAD,
                        0,
                        5
                )
        );

        assertEquals(
                ServicioCodigoError.SOLICITUD_INVALIDA.name(),
                exception.getCodigo()
        );
    }

    @Test
    void registrarDebeRechazarPrecioNegativo() {
        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> TarifaServicio.registrar(
                        crearServicio(),
                        TipoClienteTarifa.SOCIO_COMUN,
                        new BigDecimal("-1"),
                        ModalidadPrecio.POR_UNIDAD,
                        0,
                        5
                )
        );

        assertEquals(
                ServicioCodigoError.SOLICITUD_INVALIDA.name(),
                exception.getCodigo()
        );
    }

    @Test
    void registrarDebeRechazarAntiguedadMinimaMayorALaMaxima() {
        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> TarifaServicio.registrar(
                        crearServicio(),
                        TipoClienteTarifa.SOCIO_COMUN,
                        new BigDecimal("100"),
                        ModalidadPrecio.POR_UNIDAD,
                        10,
                        5
                )
        );

        assertEquals(
                ServicioCodigoError.RANGO_ANTIGUEDAD_INVALIDO.name(),
                exception.getCodigo()
        );
    }

    @Test
    void registrarDebeRechazarAntiguedadParaParticular() {
        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> TarifaServicio.registrar(
                        crearServicio(),
                        TipoClienteTarifa.PARTICULAR,
                        new BigDecimal("150"),
                        ModalidadPrecio.POR_UNIDAD,
                        0,
                        5
                )
        );

        assertEquals(
                ServicioCodigoError.ANTIGUEDAD_NO_APLICABLE_A_PARTICULAR.name(),
                exception.getCodigo()
        );
    }

    @Test
    void registrarDebeRechazarAntiguedadMinimaNegativa() {
        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> TarifaServicio.registrar(
                        crearServicio(),
                        TipoClienteTarifa.SOCIO_COMUN,
                        new BigDecimal("100"),
                        ModalidadPrecio.POR_UNIDAD,
                        -1,
                        5
                )
        );

        assertEquals(
                ServicioCodigoError.SOLICITUD_INVALIDA.name(),
                exception.getCodigo()
        );
    }

    @Test
    void modificarDebeActualizarLosDatosDeLaTarifa() {
        TarifaServicio tarifa = TarifaServicio.registrar(
                crearServicio(),
                TipoClienteTarifa.SOCIO_COMUN,
                new BigDecimal("100"),
                ModalidadPrecio.POR_UNIDAD,
                0,
                5
        );

        tarifa.modificar(
                TipoClienteTarifa.SOCIO_POLICIA,
                new BigDecimal("90"),
                ModalidadPrecio.POR_DIA,
                6,
                10
        );

        assertEquals(TipoClienteTarifa.SOCIO_POLICIA, tarifa.getTipoCliente());
        assertEquals(new BigDecimal("90"), tarifa.getPrecio());
        assertEquals(ModalidadPrecio.POR_DIA, tarifa.getModalidadPrecio());
        assertEquals(6, tarifa.getAntiguedadMinima());
        assertEquals(10, tarifa.getAntiguedadMaxima());
    }

    @Test
    void modificarConDatosInvalidosNoDebeCambiarLaTarifa() {
        TarifaServicio tarifa = TarifaServicio.registrar(
                crearServicio(),
                TipoClienteTarifa.SOCIO_COMUN,
                new BigDecimal("100"),
                ModalidadPrecio.POR_UNIDAD,
                0,
                5
        );

        assertThrows(
                ServicioValidacionException.class,
                () -> tarifa.modificar(
                        TipoClienteTarifa.PARTICULAR,
                        new BigDecimal("150"),
                        ModalidadPrecio.POR_UNIDAD,
                        1,
                        5
                )
        );

        assertEquals(TipoClienteTarifa.SOCIO_COMUN, tarifa.getTipoCliente());
        assertEquals(new BigDecimal("100"), tarifa.getPrecio());
        assertEquals(0, tarifa.getAntiguedadMinima());
        assertEquals(5, tarifa.getAntiguedadMaxima());
    }

    private Servicio crearServicio() {
        return Servicio.registrar(
                "Servicio de prueba",
                Procedencia.SEDE,
                new BigDecimal("150"),
                new BigDecimal("100"),
                ModalidadPrecio.POR_UNIDAD,
                20,
                1,
                BigDecimal.ZERO
        );
    }
}

