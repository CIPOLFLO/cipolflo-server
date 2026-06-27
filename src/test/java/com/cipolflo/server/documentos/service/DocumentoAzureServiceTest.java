package com.cipolflo.server.documentos.service;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.core.util.polling.SyncPoller;
import com.cipolflo.server.documentos.domain.DocumentoAnalizado;
import com.cipolflo.server.documentos.repository.DocumentoAnalizadoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.cipolflo.server.documentos.validation.DocumentoAzureValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocumentoAzureServiceTest {

    private DocumentIntelligenceClient client;
    private DocumentoAnalizadoRepository repository;
    private ObjectMapper objectMapper;
    private DocumentoAzureService service;
    private DocumentoAzureValidator validator;

    @BeforeEach
    void setUp() {
        client = mock(DocumentIntelligenceClient.class);
        repository = mock(DocumentoAnalizadoRepository.class);
        objectMapper = mock(ObjectMapper.class);
        validator = new DocumentoAzureValidator();

        service = new DocumentoAzureService(client, repository, objectMapper, validator);
    }

    @Test
    void deberiaLanzarErrorCuandoArchivoEsNull() {
        assertThatThrownBy(() -> service.analizarFactura(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Debe subir un archivo");

        verifyNoInteractions(client, repository);
    }

    @Test
    void deberiaLanzarErrorCuandoArchivoEstaVacio() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "factura.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThatThrownBy(() -> service.analizarFactura(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Debe subir un archivo");

        verifyNoInteractions(client, repository);
    }

    @Test
    void deberiaLanzarErrorCuandoArchivoSuperaLimite() {
        byte[] contenido = new byte[5 * 1024 * 1024];

        MultipartFile file = new MockMultipartFile(
                "file",
                "factura.pdf",
                "application/pdf",
                contenido
        );

        assertThatThrownBy(() -> service.analizarFactura(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4 MB");

        verifyNoInteractions(client, repository);
    }

    @Test
    void deberiaLanzarErrorCuandoFormatoNoEsPermitido() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "factura.txt",
                "text/plain",
                "contenido".getBytes()
        );

        assertThatThrownBy(() -> service.analizarFactura(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Formato no permitido");

        verifyNoInteractions(client, repository);
    }

    @Test
    void deberiaAnalizarFacturaYGuardarDocumento() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file",
                "factura.pdf",
                "application/pdf",
                "contenido".getBytes()
        );

        AnalyzeResult result = mock(AnalyzeResult.class);

        @SuppressWarnings("unchecked")
        SyncPoller<AnalyzeResultOperation, AnalyzeResult> poller = mock(SyncPoller.class);

        when(poller.getFinalResult()).thenReturn(result);

        when(client.beginAnalyzeDocument(
                eq("prebuilt-invoice"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(AnalyzeDocumentRequest.class)
        )).thenReturn(poller);

        when(objectMapper.writeValueAsString(result)).thenReturn("{\"content\":\"factura\"}");

        when(repository.save(any(DocumentoAnalizado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentoAnalizado documento = service.analizarFactura(file);

        assertThat(documento.getNombreArchivo()).isEqualTo("factura.pdf");
        assertThat(documento.getTipoContenido()).isEqualTo("application/pdf");
        assertThat(documento.getModeloUsado()).isEqualTo("prebuilt-invoice");
        assertThat(documento.getResultadoJson()).isEqualTo("{\"content\":\"factura\"}");
        assertThat(documento.getFechaAnalisis()).isNotNull();

        ArgumentCaptor<DocumentoAnalizado> captor = ArgumentCaptor.forClass(DocumentoAnalizado.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getNombreArchivo()).isEqualTo("factura.pdf");
    }

    @Test
    void deberiaLanzarRuntimeExceptionCuandoAzureFalla() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "factura.pdf",
                "application/pdf",
                "contenido".getBytes()
        );

        when(client.beginAnalyzeDocument(
                eq("prebuilt-invoice"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(AnalyzeDocumentRequest.class)
        )).thenThrow(new RuntimeException("Error Azure"));

        assertThatThrownBy(() -> service.analizarFactura(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo analizar");

        verify(repository, never()).save(any());
    }
}