package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.dto.ImportacionSociosResponseDto;
import com.cipolflo.server.shared.export.ArchivoExportado;
import org.springframework.web.multipart.MultipartFile;

public interface IImportacionSociosService {

    ImportacionSociosResponseDto importarSocios(MultipartFile archivo);

    ArchivoExportado generarPlantilla();
}
