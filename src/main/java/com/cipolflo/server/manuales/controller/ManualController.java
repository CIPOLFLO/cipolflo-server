package com.cipolflo.server.manuales.controller;

import com.cipolflo.server.manuales.domain.CategoriaManual;
import com.cipolflo.server.manuales.domain.Manual;
import com.cipolflo.server.manuales.dto.ManualResponseDto;
import com.cipolflo.server.manuales.service.IManualService;
import com.cipolflo.server.manuales.service.ManualDescarga;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/manuales")
public class ManualController {

    private final IManualService manualService;

    public ManualController(IManualService manualService) {
        this.manualService = manualService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ManualResponseDto>> listarManuales(
            @RequestParam(required = false) CategoriaManual categoria) {

        return ResponseEntity.ok(manualService.listarManuales(categoria));
    }

    /**
     * Devuelve el PDF de un manual.
     *
     * @param descargar {@code true} fuerza la descarga; por defecto se abre en el visor
     *                  del navegador
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{clave}")
    public ResponseEntity<Resource> descargarManual(
            @PathVariable @NotBlank(message = "Debe indicar la clave del manual") String clave,
            @RequestParam(defaultValue = "false") boolean descargar) {

        ManualDescarga descarga = manualService.descargarManual(clave);
        Manual manual = descarga.manual();

        ContentDisposition contentDisposition = (descargar
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename(manual.getNombreArchivoDescarga(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(descarga.contenido());
    }
}
