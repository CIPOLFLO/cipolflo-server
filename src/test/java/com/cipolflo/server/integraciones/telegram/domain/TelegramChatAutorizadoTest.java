package com.cipolflo.server.integraciones.telegram.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramChatAutorizadoTest {

    @Test
    void registrar_creaElChatActivoConLosDatosRecibidos() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(12345L, "Juan", false);

        assertEquals(12345L, chat.getChatId());
        assertEquals("Juan", chat.getAlias());
        assertTrue(chat.getActivo());
        assertFalse(chat.getRecibeNotificaciones());
    }

    @Test
    void registrar_conRecibeNotificacionesNulo_defaultTrue() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(12345L, "Juan", null);

        assertTrue(chat.getRecibeNotificaciones());
    }

    @Test
    void modificar_actualizaAliasYRecibeNotificaciones() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(12345L, "Juan", true);

        chat.modificar("Juan Pérez", false);

        assertEquals("Juan Pérez", chat.getAlias());
        assertFalse(chat.getRecibeNotificaciones());
    }

    @Test
    void modificar_noModificaElChatId() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(12345L, "Juan", true);

        chat.modificar("Juan Pérez", false);

        assertEquals(12345L, chat.getChatId());
    }

    @Test
    void activar_poneActivoEnTrue() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(12345L, "Juan", true);
        chat.desactivar();

        chat.activar();

        assertTrue(chat.getActivo());
    }

    @Test
    void desactivar_poneActivoEnFalse() {
        TelegramChatAutorizado chat = TelegramChatAutorizado.registrar(12345L, "Juan", true);

        chat.desactivar();

        assertFalse(chat.getActivo());
    }
}
