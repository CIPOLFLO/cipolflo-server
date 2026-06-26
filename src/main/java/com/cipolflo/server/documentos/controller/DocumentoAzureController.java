package com.cipolflo.server.documentos.controller;

import com.cipolflo.server.documentos.model.DocumentoAnalizado;
import com.cipolflo.server.documentos.service.DocumentoAzureService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/api/v1/documentos")
public class DocumentoAzureController {

    private final DocumentoAzureService service;

    public DocumentoAzureController(DocumentoAzureService service) {
        this.service = service;
    }

    /**
     * POST /api/documentos/analizar-factura
     * Recibe un archivo multipart, lo analiza con Azure y devuelve el resultado.
     *
     * Body: form-data con campo "file" conteniendo el archivo.
     * Formatos aceptados: PDF, JPG, JPEG, PNG, BMP, TIFF, HEIF
     */

    @PostMapping(
            value = "/analizar-factura",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentoAnalizado> analizarFactura(
            @RequestParam("file") MultipartFile file) {
        DocumentoAnalizado resultado = service.analizarFactura(file);
        return ResponseEntity.ok(resultado);
    }

}

