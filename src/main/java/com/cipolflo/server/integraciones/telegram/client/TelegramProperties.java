package com.cipolflo.server.integraciones.telegram.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciales del bot de Telegram. Se bindean desde {@code cipolflo.telegram.*} en
 * application.properties, leídas ahí desde variables de entorno — nunca hardcodeadas.
 */
@ConfigurationProperties(prefix = "cipolflo.telegram")
public record TelegramProperties(
        String botToken,
        String webhookSecret
) {
}
