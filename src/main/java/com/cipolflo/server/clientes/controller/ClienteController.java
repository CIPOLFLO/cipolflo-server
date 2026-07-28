package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.dto.*;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.clientes.service.IImportacionSociosService;
import com.cipolflo.server.clientes.service.IRegistroParticularService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@Validated
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private static final Set<String> CAMPOS_ORDEN_PERMITIDOS = Set.of(
            "nombreCompleto", "cedula", "numeroSocio"
    );

    private final IClienteService clienteService;
    private final IRegistroParticularService registroParticularService;
    private final IImportacionSociosService importacionSociosService;

    public ClienteController(
            IClienteService clienteService,
            IRegistroParticularService registroParticularService,
            IImportacionSociosService importacionSociosService) {
        this.clienteService = clienteService;
        this.registroParticularService = registroParticularService;
        this.importacionSociosService = importacionSociosService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageResponse<ListadoClientesResponseDto>> getListadoClientes(
            @Valid @ModelAttribute ListadoClientesRequestDto filtros,
            @Valid @ModelAttribute PageRequestDto pageRequest) {
        if (pageRequest.sortField() != null
                && !CAMPOS_ORDEN_PERMITIDOS.contains(pageRequest.sortField())) {
            throw new IllegalArgumentException(
                    "sortField inválido. Valores permitidos: " + CAMPOS_ORDEN_PERMITIDOS);
        }
        return ResponseEntity.ok(clienteService.getListadoClientes(filtros, pageRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> getDetalleCliente(
            @PathVariable @Positive(message = "El id del cliente debe ser un número positivo") Long id) {
        return ResponseEntity.ok(clienteService.getDetalleCliente(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/socios/{id}/estado")
    public ResponseEntity<EstadoSocioResponseDto> consultarEstadoSocio(
            @PathVariable
            @Positive(message = "El id del socio debe ser un número positivo")
            Long id) {
        return ResponseEntity.ok(clienteService.consultarEstadoSocio(id));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/socios/{id}/baja")
    public ResponseEntity<Void> darDeBajaSocio(
            @PathVariable
            @Positive(message = "El id del socio debe ser un número positivo")
            Long id) {

        clienteService.darDeBajaSocio(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/particulares/{id}")
    public ResponseEntity<ClienteResponseDto> modificarParticular(
            @PathVariable @Positive(message = "El id del cliente debe ser un número positivo") Long id,
            @Valid @RequestBody ModificacionParticularRequestDto dto) {
        return ResponseEntity.ok(clienteService.modificarParticular(id, dto));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/socios/{id}")
    public ResponseEntity<ClienteResponseDto> modificarSocio(
            @PathVariable @Positive(message = "El id del cliente debe ser un número positivo") Long id,
            @Valid @RequestBody ModificacionSocioRequestDto dto) {
        return ResponseEntity.ok(clienteService.modificarSocio(id, dto));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/socios")
    public ResponseEntity<ClienteResponseDto> registrarSocio(
            @Valid @RequestBody RegistroSocioRequestDto dto) {

        ClienteResponseDto response = clienteService.registrarSocio(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/empresas")
    public ResponseEntity<ClienteResponseDto> registrarEmpresa(
            @Valid @RequestBody RegistroEmpresaRequestDto dto) {

        ClienteResponseDto response = clienteService.registrarEmpresa(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<BusquedaCedulaResponseDto> buscarPorCedula(@PathVariable String cedula) {
        return ResponseEntity.ok(
            clienteService.buscarPorCedula(cedula)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/particulares")
    public ResponseEntity<ClienteResponseDto> registrarParticular(
            @Valid @RequestBody RegistroParticularRequestDto dto) {

        ClienteResponseDto response = registroParticularService.registrarParticular(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/exportar")
    public ResponseEntity<byte[]> exportarClientes(
        @Valid @RequestBody ListadoClientesRequestDto filtros){
            ArchivoExportado archivo = clienteService.exportarClientes(filtros);
            return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getNombre() + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(archivo.getContenido());
        }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/socios/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacionSociosResponseDto> importarSocios(
            @RequestParam("file") MultipartFile file) {

        ImportacionSociosResponseDto response = importacionSociosService.importarSocios(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/socios/importar/plantilla")
    public ResponseEntity<byte[]> descargarPlantillaImportacionSocios() {
        ArchivoExportado archivo = importacionSociosService.generarPlantilla();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getNombre() + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo.getContenido());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/rut/{rut}")
    public ResponseEntity<BusquedaRutResponseDto> buscarPorRut(@PathVariable String rut) {
        return ResponseEntity.ok(
            clienteService.buscarPorRut(rut)
        );
    }
    @PreAuthorize("isAuthenticated()")
@GetMapping("/socios/{id}/comprobante")
public ResponseEntity<byte[]> descargarComprobanteAltaSocio(
        @PathVariable @Positive(message = "El id del socio debe ser un número positivo") Long id) {
    ArchivoExportado archivo = clienteService.generarComprobanteAltaSocio(id);
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getNombre() + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(archivo.getContenido());
}
}
