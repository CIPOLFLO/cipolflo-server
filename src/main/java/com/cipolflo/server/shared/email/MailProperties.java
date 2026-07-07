package com.cipolflo.server.shared.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Datos del remitente de la aplicación. Se bindean desde {@code cipolflo.mail.*}
 * en application.properties.
 */
@ConfigurationProperties(prefix = "cipolflo.mail")
public record MailProperties(
        String from,
        String nombreRemitente
) {
}
