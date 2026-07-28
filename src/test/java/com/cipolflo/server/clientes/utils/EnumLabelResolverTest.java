package com.cipolflo.server.clientes.utils;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EnumLabelResolverTest {

    @Test
    void deberiaResolverPorLabelIgnorandoMayusculasYEspacios() {
        Optional<EstadoSocio> resultado = EnumLabelResolver.resolverPorLabel(EstadoSocio.class, "  activo  ");

        assertTrue(resultado.isPresent());
        assertEquals(EstadoSocio.ACTIVO, resultado.get());
    }

    @Test
    void deberiaResolverPorLabelConEspacios() {
        Optional<EstadoSocio> resultado = EnumLabelResolver.resolverPorLabel(EstadoSocio.class, "De baja");

        assertEquals(Optional.of(EstadoSocio.DE_BAJA), resultado);
    }

    @Test
    void deberiaResolverPorNombreDeConstante() {
        Optional<EstadoSocio> resultado = EnumLabelResolver.resolverPorLabel(EstadoSocio.class, "INACTIVO");

        assertEquals(Optional.of(EstadoSocio.INACTIVO), resultado);
    }

    @Test
    void deberiaDevolverVacioCuandoNoHayMatch() {
        assertTrue(EnumLabelResolver.resolverPorLabel(EstadoSocio.class, "Suspendido").isEmpty());
    }

    @Test
    void deberiaDevolverVacioCuandoElTextoEsNuloOBlanco() {
        assertTrue(EnumLabelResolver.resolverPorLabel(EstadoSocio.class, null).isEmpty());
        assertTrue(EnumLabelResolver.resolverPorLabel(EstadoSocio.class, "   ").isEmpty());
    }
}
