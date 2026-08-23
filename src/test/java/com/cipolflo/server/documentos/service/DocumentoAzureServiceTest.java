package com.cipolflo.server.documentos.service;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.ai.documentintelligence.models.AnalyzeOperationDetails;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
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
import com.cipolflo.server.documentos.mapper.DocumentoAnalizadoMapper;
import com.cipolflo.server.documentos.dto.DocumentoAnalizadoResponseDto;

class DocumentoAzureServiceTest {

    private DocumentIntelligenceClient client;
    private DocumentoAnalizadoRepository repository;
    private ObjectMapper objectMapper;
    private DocumentoAzureService service;
    private DocumentoAzureValidator validator;
    private DocumentoAnalizadoMapper mapper;

    @BeforeEach
    void setUp() {
        client = mock(DocumentIntelligenceClient.class);
        repository = mock(DocumentoAnalizadoRepository.class);
        objectMapper = mock(ObjectMapper.class);
        validator = new DocumentoAzureValidator();
        mapper = new DocumentoAnalizadoMapper();
        service = new DocumentoAzureService(client, repository, objectMapper, validator, mapper);
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
                "%PDF-1.4 contenido".getBytes()
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
                "%PDF-1.4 contenido".getBytes()
        );

        AnalyzeResult result = mock(AnalyzeResult.class);

        @SuppressWarnings("unchecked")
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> poller = mock(SyncPoller.class);

        when(poller.getFinalResult()).thenReturn(result);

        when(client.beginAnalyzeDocument(
                eq("prebuilt-invoice"),
                any(AnalyzeDocumentOptions.class)
        )).thenReturn(poller);

        when(objectMapper.writeValueAsString(result)).thenReturn("{\"content\":\"factura\"}");

        when(repository.save(any(DocumentoAnalizado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentoAnalizadoResponseDto   documento = service.analizarFactura(file);

        assertThat(documento.nombreArchivo()).isEqualTo("factura.pdf");
        assertThat(documento.tipoContenido()).isEqualTo("application/pdf");
        assertThat(documento.modeloUsado()).isEqualTo("prebuilt-invoice");
        assertThat(documento.resultadoJson()).isEqualTo("{\"content\":\"factura\"}");
        assertThat(documento.fechaAnalisis()).isNotNull();

        ArgumentCaptor<DocumentoAnalizado> captor = ArgumentCaptor.forClass(DocumentoAnalizado.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getNombreArchivo()).isEqualTo("factura.pdf");

        // El contenido del archivo viaja dentro de AnalyzeDocumentOptions
        ArgumentCaptor<AnalyzeDocumentOptions> optionsCaptor =
                ArgumentCaptor.forClass(AnalyzeDocumentOptions.class);
        verify(client).beginAnalyzeDocument(eq("prebuilt-invoice"), optionsCaptor.capture());

        assertThat(optionsCaptor.getValue().getBytesSource()).isEqualTo(file.getBytes());
    }

    @Test
    void deberiaLanzarRuntimeExceptionCuandoAzureFalla() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "factura.pdf",
                "application/pdf",
                "%PDF-1.4 contenido".getBytes()
        );

        when(client.beginAnalyzeDocument(
                eq("prebuilt-invoice"),
                any(AnalyzeDocumentOptions.class)
        )).thenThrow(new RuntimeException("Error Azure"));

        assertThatThrownBy(() -> service.analizarFactura(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo analizar");

        verify(repository, never()).save(any());
    }
}