package com.cipolflo.server.integraciones.telegram.processor;

import com.cipolflo.server.integraciones.telegram.dto.TelegramUpdateDto;

public interface IProcesadorMensajeTelegram {

    /** Procesa un update de Telegram de forma asíncrona respecto de la respuesta HTTP del webhook. */
    void procesar(TelegramUpdateDto update);
}
