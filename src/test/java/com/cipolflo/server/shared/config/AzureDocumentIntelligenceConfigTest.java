package com.cipolflo.server.shared.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AzureDocumentIntelligenceConfigTest {

    @Test
    void deberiaCrearClienteAzure() {
        AzureDocumentIntelligenceProperties properties =
                new AzureDocumentIntelligenceProperties(
                        "https://dummy.cognitiveservices.azure.com/",
                        "dummy-key"
                );

        AzureDocumentIntelligenceConfig config = new AzureDocumentIntelligenceConfig();

        DocumentIntelligenceClient client = config.documentIntelligenceClient(properties);

        assertNotNull(client);
    }
}
