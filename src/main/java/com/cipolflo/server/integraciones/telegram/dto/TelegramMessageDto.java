package com.cipolflo.server.integraciones.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessageDto(String text, TelegramChatDto chat) {
}
