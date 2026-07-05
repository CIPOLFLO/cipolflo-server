package com.cipolflo.server.documentos.service;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.core.util.polling.SyncPoller;
import com.cipolflo.server.documentos.domain.DocumentoAnalizado;
import com.cipolflo.server.documentos.dto.DocumentoAnalizadoResponseDto;
import com.cipolflo.server.documentos.mapper.DocumentoAnalizadoMapper;
import com.cipolflo.server.documentos.repository.DocumentoAnalizadoRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cipolflo.server.documentos.validation.DocumentoAzureValidator;

import java.nio.file.Paths;
import java.time.LocalDate;



@Service
public class DocumentoAzureService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoAzureService.class);
    private static final String MODELO_FACTURA = "prebuilt-invoice";
    private final DocumentIntelligenceClient client;
    private final DocumentoAnalizadoRepository repository;
    private final ObjectMapper objectMapper;
    private final DocumentoAzureValidator validator;
    private final DocumentoAnalizadoMapper mapper;

    public DocumentoAzureService(

            DocumentIntelligenceClient client,
            DocumentoAnalizadoRepository repository,
            ObjectMapper objectMapper,
            DocumentoAzureValidator validator,
            DocumentoAnalizadoMapper mapper
    ) {
        this.client = client;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.mapper = mapper;
    }

    public DocumentoAnalizadoResponseDto analizarFactura(MultipartFile file) {
        try {
            validator.validarArchivo(file);
            log.info("Iniciando análisis de factura.");

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

            int documentosDetectados = result.getDocuments() == null ? 0 : result.getDocuments().size();
            log.info("Análisis completado. Documentos detectados: {}", documentosDetectados);

            // Construimos la entidad para persistir
            DocumentoAnalizado documento = new DocumentoAnalizado();
            documento.setNombreArchivo(sanitizarNombreArchivo(file.getOriginalFilename()));
            documento.setTipoContenido(file.getContentType());
            documento.setModeloUsado(MODELO_FACTURA);
            documento.setFechaAnalisis(LocalDate.now(ZonaHoraria.URUGUAY));
            documento.setResultadoJson(objectMapper.writeValueAsString(result));
            DocumentoAnalizado documentoGuardado = repository.save(documento);
            return mapper.toResponseDto(documentoGuardado);
        }catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al analizar documento con Azure: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo analizar el documento con Azure Document Intelligence", e);
        }
    }
    private String sanitizarNombreArchivo(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            return "archivo_sin_nombre";
        }

        String nombre = Paths.get(nombreOriginal)
                .getFileName()
                .toString()
                .replaceAll("[\\\\x00-\\\\x1F\\\\x7F<>\"'&]", "_")
                .strip();

        if (nombre.length() > 255) {
            nombre = nombre.substring(0, 255);
        }

        if (nombre.isBlank()) {
            return "archivo_sin_nombre";
        }

        return nombre;
    }
}

