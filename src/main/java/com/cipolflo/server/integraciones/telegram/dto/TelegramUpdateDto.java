package com.cipolflo.server.integraciones.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mapea solo los campos del update de Telegram que usamos. {@code @JsonIgnoreProperties}
 * porque Telegram agrega campos con frecuencia y un campo nuevo no debe romper el webhook.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUpdateDto(
        @JsonProperty("update_id") Long updateId,
        TelegramMessageDto message
) {
}
