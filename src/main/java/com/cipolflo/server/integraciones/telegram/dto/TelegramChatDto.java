package com.cipolflo.server.integraciones.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramChatDto(Long id, String username) {
}
