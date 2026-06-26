package com.cipolflo.server.documentos.service;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.core.util.polling.SyncPoller;
import com.cipolflo.server.documentos.model.DocumentoAnalizado;
import com.cipolflo.server.documentos.repository.DocumentoAnalizadoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;


@Service
public class DocumentoAzureService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoAzureService.class);
    private static final String MODELO_FACTURA = "prebuilt-invoice";
    private final DocumentIntelligenceClient client;
    private final DocumentoAnalizadoRepository repository;
    private final ObjectMapper objectMapper;

    public DocumentoAzureService(

            DocumentIntelligenceClient client,
            DocumentoAnalizadoRepository repository,
            ObjectMapper objectMapper
    ) {

        this.client = client;
        this.repository = repository;
        this.objectMapper = objectMapper;

    }
    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe subir un archivo.");
        }

        long maxSizeBytes = 4L * 1024 * 1024;

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("El archivo supera el límite de 4 MB permitido.");
        }

        String contentType = file.getContentType();

        List<String> tiposPermitidos = List.of(
                "application/pdf",
                "image/jpeg",
                "image/png",
                "image/bmp",
                "image/tiff",
                "image/heif"
        );

        if (contentType == null || !tiposPermitidos.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Formato no permitido: " + contentType + ". Use PDF, JPG, PNG, BMP, TIFF o HEIF."
            );
        }
    }

    public DocumentoAnalizado analizarFactura(MultipartFile file) {
        try {
            log.info("Iniciando análisis de factura: {}", file.getOriginalFilename());

            // Leemos los bytes del archivo y creamos el request de Azure
            byte[] fileBytes = file.getBytes();
            AnalyzeDocumentRequest request = new AnalyzeDocumentRequest();
            request.setBase64Source(fileBytes);

            // Enviamos el documento a Azure y esperamos el resultado
            // beginAnalyzeDocument devuelve un SyncPoller (operación long-running)

            SyncPoller<AnalyzeResultOperation, AnalyzeResult> poller =
                    client.beginAnalyzeDocument(
                            MODELO_FACTURA,  // modelo precompilado
                            null,            // pages (null = todas)
                            null,            // locale (null = auto-detect)
                            null,            // stringIndexType
                            null,            // features adicionales
                            null,            // queryFields
                            null,            // outputContentFormat
                            null,            // output adicional
                            request

                    );

            // getFinalResult() bloquea hasta que Azure termina el análisis
            AnalyzeResult result = poller.getFinalResult();

            log.info("Análisis completado. Documentos detectados: {}", result.getDocuments().size());

            // Construimos la entidad para persistir
            DocumentoAnalizado documento = new DocumentoAnalizado();
            documento.setNombreArchivo(file.getOriginalFilename());
            documento.setTipoContenido(file.getContentType());
            documento.setModeloUsado(MODELO_FACTURA);
            documento.setFechaAnalisis(Instant.now());
            documento.setResultadoJson(objectMapper.writeValueAsString(result));
            return repository.save(documento);
        }catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al analizar documento con Azure: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo analizar el documento con Azure Document Intelligence", e);
        }
    }
}

