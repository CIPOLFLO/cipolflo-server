package com.cipolflo.server.documentos.controller;


import com.cipolflo.server.documentos.dto.DocumentoAnalizadoResponseDto;
import com.cipolflo.server.documentos.service.DocumentoAzureService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;


import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DocumentoAzureControllerTest {

    private final DocumentoAzureService service = mock(DocumentoAzureService.class);
    private final DocumentoAzureController controller = new DocumentoAzureController(service);

    @Test
    void deberiaAnalizarFacturaYRetornarOk() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "factura.pdf",
                "application/pdf",
                "contenido".getBytes()
        );

        DocumentoAnalizadoResponseDto documento = new DocumentoAnalizadoResponseDto(
                1L,
                "factura.pdf",
                "application/pdf",
                "prebuilt-invoice",
                LocalDate.now(),
                "{}"
        );

        when(service.analizarFactura(file)).thenReturn(documento);

        ResponseEntity<DocumentoAnalizadoResponseDto> response = controller.analizarFactura(file);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(documento);
        verify(service).analizarFactura(file);
    }
}