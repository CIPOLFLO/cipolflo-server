package com.cipolflo.server.shared.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del cliente de Azure Document Intelligence.
 * Crea un bean singleton que se inyecta en DocumentoAzureService.
 */

@Configuration
public class AzureDocumentIntelligenceConfig {

    @Bean
    public DocumentIntelligenceClient documentIntelligenceClient(
            AzureDocumentIntelligenceProperties properties
    ) {
        return new DocumentIntelligenceClientBuilder()
                .endpoint(properties.endpoint())
                .credential(new AzureKeyCredential(properties.key()))
                .buildClient();
    }
}


