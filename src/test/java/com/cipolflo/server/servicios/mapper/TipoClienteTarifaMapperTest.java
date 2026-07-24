package com.cipolflo.server.servicios.mapper;

import com.cipolflo.server.clientes.domain.Empresa;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TipoClienteTarifaMapperTest {

    @Test
    void particularDebeMapearAParticular() {
        Particular particular = new Particular();

        TipoClienteTarifa resultado =
                TipoClienteTarifaMapper.desdeCliente(particular);

        assertEquals(TipoClienteTarifa.PARTICULAR, resultado);
    }

    @Test
    void empresaDebeMapearAParticular() {
        Empresa empresa = new Empresa();

        TipoClienteTarifa resultado =
                TipoClienteTarifaMapper.desdeCliente(empresa);

        assertEquals(TipoClienteTarifa.PARTICULAR, resultado);
    }

    @Test
    void socioComunDebeMapearASocioComun() {
        Socio socio = crearSocio(CategoriaSocio.SOCIO_COMUN);

        TipoClienteTarifa resultado =
                TipoClienteTarifaMapper.desdeCliente(socio);

        assertEquals(TipoClienteTarifa.SOCIO_COMUN, resultado);
    }

    @Test
    void policiaActivoDebeMapearASocioPolicia() {
        Socio socio = crearSocio(CategoriaSocio.POLICIA_ACTIVO);

        TipoClienteTarifa resultado =
                TipoClienteTarifaMapper.desdeCliente(socio);

        assertEquals(TipoClienteTarifa.SOCIO_POLICIA, resultado);
    }

    @Test
    void policiaRetiradoDebeMapearASocioPoliciaRetirado() {
        Socio socio = crearSocio(CategoriaSocio.POLICIA_RETIRADO);

        TipoClienteTarifa resultado =
                TipoClienteTarifaMapper.desdeCliente(socio);

        assertEquals(
                TipoClienteTarifa.SOCIO_POLICIA_RETIRADO,
                resultado
        );
    }

    @Test
    void socioSinCategoriaDebeLanzarExcepcion() {
        Socio socio = new Socio();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TipoClienteTarifaMapper.desdeCliente(socio)
        );

        assertEquals(
                "La categoría del socio no puede ser nula",
                exception.getMessage()
        );
    }

    private Socio crearSocio(CategoriaSocio categoriaSocio) {
        Socio socio = new Socio();
        socio.setCategoriaSocio(categoriaSocio);
        return socio;
    }
}
