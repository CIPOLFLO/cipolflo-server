package com.cipolflo.server.integraciones.mensajeria.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SesionesConversacionTest {

    @Mock
    private ChatMemory chatMemory;

    private Instant ahora = Instant.parse("2026-01-01T10:00:00Z");

    private final Clock clock = new Clock() {
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    };

    private SesionesConversacion sesionesConversacion;

    @BeforeEach
    void setUp() {
        MemoriaConversacionalProperties properties = new MemoriaConversacionalProperties(20, Duration.ofMinutes(30));
        sesionesConversacion = new SesionesConversacion(chatMemory, properties, clock);
    }

    @Test
    void dentroDelTtl_noDeberiaLimpiarLaMemoria() {
        sesionesConversacion.registrarActividad("chat-1");

        ahora = ahora.plus(Duration.ofMinutes(29));
        sesionesConversacion.registrarActividad("chat-1");

        verify(chatMemory, never()).clear("chat-1");
    }

    @Test
    void pasadoElTtl_deberiaLimpiarLaMemoria() {
        sesionesConversacion.registrarActividad("chat-1");

        ahora = ahora.plus(Duration.ofMinutes(31));
        sesionesConversacion.registrarActividad("chat-1");

        verify(chatMemory).clear("chat-1");
    }

    @Test
    void primerMensajeDeUnaConversacion_noDeberiaLimpiarNada() {
        sesionesConversacion.registrarActividad("chat-nuevo");

        verify(chatMemory, never()).clear("chat-nuevo");
    }

    @Test
    void chatsDistintos_deberianTenerTtlIndependientes() {
        sesionesConversacion.registrarActividad("chat-1");

        ahora = ahora.plus(Duration.ofMinutes(31));
        sesionesConversacion.registrarActividad("chat-2");

        verify(chatMemory, never()).clear("chat-2");
        verify(chatMemory, never()).clear("chat-1");
    }
}
