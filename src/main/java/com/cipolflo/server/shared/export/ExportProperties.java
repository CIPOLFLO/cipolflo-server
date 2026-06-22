package com.cipolflo.server.shared.export;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cipolflo.export")
public record ExportProperties(int maxFilas,long maxBytes) {
    
}
