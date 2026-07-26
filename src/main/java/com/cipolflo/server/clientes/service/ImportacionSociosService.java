package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.dto.FilaErrorImportacionDto;
import com.cipolflo.server.clientes.dto.ImportacionSocioDto;
import com.cipolflo.server.clientes.dto.ImportacionSociosResponseDto;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import com.cipolflo.server.clientes.utils.EnumLabelResolver;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.export.IExportService;
import com.cipolflo.server.shared.export.NombreArchivoExport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cada fila se procesa de forma independiente: un dato repetido (cédula, email o número de
 * socio) o inválido no aborta el archivo, se reporta en detalleErrores y se sigue con las
 * siguientes filas. No lleva @Transactional a nivel de método a propósito: cada
 * saveAndFlush corre en su propia transacción (comportamiento por defecto de Spring Data
 * JPA), así una fila que falla al persistir no deja la sesión de Hibernate en un estado
 * inválido para las filas siguientes.
 */
@Service
public class ImportacionSociosService implements IImportacionSociosService {

    private static final int COLUMNA_NUMERO_SOCIO = 0;
    private static final int COLUMNA_CEDULA = 1;
    private static final int COLUMNA_NOMBRE_COMPLETO = 2;
    private static final int COLUMNA_FECHA_NACIMIENTO = 3;
    private static final int COLUMNA_TELEFONO = 4;
    private static final int COLUMNA_EMAIL = 5;
    private static final int COLUMNA_METODO_COBRO = 6;
    private static final int COLUMNA_PAIS = 7;
    private static final int COLUMNA_DEPARTAMENTO = 8;
    private static final int COLUMNA_CIUDAD = 9;
    private static final int COLUMNA_DIRECCION = 10;
    private static final int COLUMNA_OBSERVACIONES = 11;
    private static final int COLUMNA_CATEGORIA_SOCIO = 12;
    private static final int COLUMNA_FECHA_INGRESO = 13;
    private static final int COLUMNA_ESTADO = 14;
    private static final int COLUMNAS_ESPERADAS = 15;

    private static final List<DateTimeFormatter> FORMATOS_FECHA = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );

    private static final List<String> ENCABEZADOS_PLANTILLA = List.of(
            "numeroSocio", "cedula", "nombreCompleto", "fechaNacimiento (dd/MM/yyyy)", "telefono",
            "email", "metodoCobro", "pais", "departamento", "ciudad", "direccion",
            "observaciones", "categoriaSocio", "fechaIngreso (dd/MM/yyyy)", "estado"
    );

    private static final int[] ANCHOS_PLANTILLA = {
            3500, 4000, 8000, 6000, 4000,
            6000, 5000, 4000, 4500, 4000, 8000,
            6000, 5500, 6000, 3500
    };

    private final ClienteRepository clienteRepository;
    private final Validator validator;
    private final IExportService exportService;

    public ImportacionSociosService(
            ClienteRepository clienteRepository,
            Validator validator,
            IExportService exportService
    ) {
        this.clienteRepository = clienteRepository;
        this.validator = validator;
        this.exportService = exportService;
    }

    @Override
    public ArchivoExportado generarPlantilla() {
        List<List<String>> filasEjemplo = List.of(
                List.of(
                        "101", "12345678", "Juan Pérez", "15/03/1985", "099123456",
                        "juan.perez@mail.com", "Efectivo", "Uruguay", "Montevideo", "Montevideo",
                        "Av. Italia 1234", "", "Socio común", "01/06/2015", "Activo"
                ),
                List.of(
                        "102", "87654321", "María Rodríguez", "22/11/1978", "098765432",
                        "", "Transferencia", "Uruguay", "Canelones", "Las Piedras",
                        "", "", "", "10/09/2018", "Inactivo"
                )
        );

        byte[] contenido = exportService.generarExcel(
                "Socios",
                ENCABEZADOS_PLANTILLA,
                filasEjemplo,
                ANCHOS_PLANTILLA
        );

        return new ArchivoExportado(NombreArchivoExport.generar("plantilla_importacion_socios"), contenido);
    }

    @Override
    public ImportacionSociosResponseDto importarSocios(MultipartFile archivo) {
        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet hoja = workbook.getSheetAt(0);
            validarEncabezado(hoja);
            if (hoja.getLastRowNum() < 1) {
                throw new ClienteValidacionException(
                        ClienteCodigoError.ARCHIVO_IMPORTACION_INVALIDO.name(),
                        "El archivo no contiene filas de datos"
                );
            }
            return procesarFilas(hoja);
        } catch (IOException e) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.ARCHIVO_IMPORTACION_INVALIDO.name(),
                    "El archivo no es un Excel válido"
            );
        }
    }

    private void validarEncabezado(Sheet hoja) {
        Row encabezado = hoja.getRow(0);
        if (encabezado == null || encabezado.getLastCellNum() < COLUMNAS_ESPERADAS) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.ARCHIVO_IMPORTACION_INVALIDO.name(),
                    "El archivo no tiene el formato esperado: se requieren " + COLUMNAS_ESPERADAS + " columnas"
            );
        }
    }

    private ImportacionSociosResponseDto procesarFilas(Sheet hoja) {
        DataFormatter dataFormatter = new DataFormatter();
        Set<String> cedulasVistas = new HashSet<>();
        Set<String> emailsVistos = new HashSet<>();
        Set<Integer> numerosVistos = new HashSet<>();
        List<FilaErrorImportacionDto> errores = new ArrayList<>();
        int totalFilas = 0;
        int filasImportadas = 0;

        for (int i = 1; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);
            if (esFilaVacia(fila, dataFormatter)) continue;

            int numeroFila = i + 1;
            totalFilas++;

            try {
                ImportacionSocioDto dto = parsearFila(fila, dataFormatter);
                validarCamposObligatorios(dto);

                String cedulaNormalizada = CedulaNormalizador.normalizar(dto.getCedula());
                String mailNormalizado = dto.getEmail() != null ? dto.getEmail().trim() : null;

                FilaErrorImportacionDto errorDuplicado = detectarDuplicado(
                        dto, cedulaNormalizada, mailNormalizado,
                        cedulasVistas, emailsVistos, numerosVistos, numeroFila
                );
                if (errorDuplicado != null) {
                    errores.add(errorDuplicado);
                    continue;
                }

                Socio socio = construirSocio(dto, cedulaNormalizada, mailNormalizado);
                try {
                    clienteRepository.saveAndFlush(socio);
                } catch (DataIntegrityViolationException e) {
                    errores.add(new FilaErrorImportacionDto(
                            numeroFila,
                            ClienteCodigoError.CEDULA_DUPLICADA.name(),
                            "Ya existe un socio con esa cédula"
                    ));
                    continue;
                }

                cedulasVistas.add(cedulaNormalizada);
                if (mailNormalizado != null) emailsVistos.add(mailNormalizado.toLowerCase());
                numerosVistos.add(dto.getNumeroSocio());
                filasImportadas++;
            } catch (FilaInvalidaException e) {
                errores.add(new FilaErrorImportacionDto(numeroFila, "FILA_INVALIDA", e.getMessage()));
            }
        }

        return new ImportacionSociosResponseDto(totalFilas, filasImportadas, errores.size(), errores);
    }

    private FilaErrorImportacionDto detectarDuplicado(
            ImportacionSocioDto dto,
            String cedulaNormalizada,
            String mailNormalizado,
            Set<String> cedulasVistas,
            Set<String> emailsVistos,
            Set<Integer> numerosVistos,
            int numeroFila
    ) {
        if (cedulasVistas.contains(cedulaNormalizada) || clienteRepository.existsByCedula(cedulaNormalizada)) {
            return new FilaErrorImportacionDto(
                    numeroFila,
                    ClienteCodigoError.CEDULA_DUPLICADA.name(),
                    "Ya existe un socio con la cédula " + dto.getCedula()
            );
        }
        if (mailNormalizado != null) {
            String mailEnMinusculas = mailNormalizado.toLowerCase();
            if (emailsVistos.contains(mailEnMinusculas) || clienteRepository.existsByMailIgnoreCase(mailNormalizado)) {
                return new FilaErrorImportacionDto(
                        numeroFila,
                        ClienteCodigoError.EMAIL_DUPLICADO.name(),
                        "Ya existe un socio con el email " + mailNormalizado
                );
            }
        }
        if (numerosVistos.contains(dto.getNumeroSocio()) || clienteRepository.existsByNumeroSocio(dto.getNumeroSocio())) {
            return new FilaErrorImportacionDto(
                    numeroFila,
                    ClienteCodigoError.NUMERO_SOCIO_DUPLICADO.name(),
                    "Ya existe un socio con el número " + dto.getNumeroSocio()
            );
        }
        return null;
    }

    private Socio construirSocio(ImportacionSocioDto dto, String cedulaNormalizada, String mailNormalizado) {
        Socio socio = Socio.registrar(
                cedulaNormalizada,
                dto.getNombreCompleto(),
                dto.getTelefono(),
                mailNormalizado,
                dto.getObservaciones(),
                dto.getFechaNacimiento(),
                dto.getPais(),
                dto.getDepartamento(),
                dto.getCiudad(),
                dto.getDireccion(),
                dto.getMetodoCobro(),
                dto.getCategoriaSocio(),
                dto.getFechaIngreso()
        );
        socio.setNumeroSocio(dto.getNumeroSocio());
        socio.setEstado(dto.getEstado());
        return socio;
    }

    private void validarCamposObligatorios(ImportacionSocioDto dto) {
        Set<ConstraintViolation<ImportacionSocioDto>> violaciones = validator.validate(dto);
        if (!violaciones.isEmpty()) {
            throw new FilaInvalidaException(violaciones.iterator().next().getMessage());
        }
    }

    private ImportacionSocioDto parsearFila(Row fila, DataFormatter dataFormatter) {
        ImportacionSocioDto dto = new ImportacionSocioDto();
        dto.setNumeroSocio(parseEntero(fila, COLUMNA_NUMERO_SOCIO, dataFormatter));
        dto.setCedula(parseTexto(fila, COLUMNA_CEDULA, dataFormatter));
        dto.setNombreCompleto(parseTexto(fila, COLUMNA_NOMBRE_COMPLETO, dataFormatter));
        dto.setFechaNacimiento(parseFecha(fila, COLUMNA_FECHA_NACIMIENTO, dataFormatter));
        dto.setTelefono(parseTexto(fila, COLUMNA_TELEFONO, dataFormatter));
        dto.setEmail(parseTexto(fila, COLUMNA_EMAIL, dataFormatter));
        dto.setMetodoCobro(parseEnum(fila, COLUMNA_METODO_COBRO, dataFormatter, MetodoCobro.class, "método de cobro"));
        dto.setPais(parseTexto(fila, COLUMNA_PAIS, dataFormatter));
        dto.setDepartamento(parseTexto(fila, COLUMNA_DEPARTAMENTO, dataFormatter));
        dto.setCiudad(parseTexto(fila, COLUMNA_CIUDAD, dataFormatter));
        dto.setDireccion(parseTexto(fila, COLUMNA_DIRECCION, dataFormatter));
        dto.setObservaciones(parseTexto(fila, COLUMNA_OBSERVACIONES, dataFormatter));
        CategoriaSocio categoriaSocio = parseEnum(fila, COLUMNA_CATEGORIA_SOCIO, dataFormatter, CategoriaSocio.class, "categoría de socio");
        dto.setCategoriaSocio(categoriaSocio != null ? categoriaSocio : CategoriaSocio.SOCIO_COMUN);
        dto.setFechaIngreso(parseFecha(fila, COLUMNA_FECHA_INGRESO, dataFormatter));
        dto.setEstado(parseEnum(fila, COLUMNA_ESTADO, dataFormatter, EstadoSocio.class, "estado"));
        return dto;
    }

    private boolean esFilaVacia(Row fila, DataFormatter dataFormatter) {
        if (fila == null) return true;
        for (Cell celda : fila) {
            if (celda != null && !dataFormatter.formatCellValue(celda).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String parseTexto(Row fila, int columna, DataFormatter dataFormatter) {
        Cell celda = fila.getCell(columna);
        if (celda == null) return null;
        String valor = dataFormatter.formatCellValue(celda).trim();
        return valor.isEmpty() ? null : valor;
    }

    private Integer parseEntero(Row fila, int columna, DataFormatter dataFormatter) {
        Cell celda = fila.getCell(columna);
        if (celda == null) return null;
        if (celda.getCellType() == CellType.NUMERIC) {
            return (int) celda.getNumericCellValue();
        }
        String valor = dataFormatter.formatCellValue(celda).trim();
        if (valor.isEmpty()) return null;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new FilaInvalidaException("Número de socio inválido: '" + valor + "'");
        }
    }

    private LocalDate parseFecha(Row fila, int columna, DataFormatter dataFormatter) {
        Cell celda = fila.getCell(columna);
        if (celda == null || celda.getCellType() == CellType.BLANK) return null;
        if (celda.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celda)) {
            return celda.getLocalDateTimeCellValue().toLocalDate();
        }
        String valor = dataFormatter.formatCellValue(celda).trim();
        if (valor.isEmpty()) return null;
        for (DateTimeFormatter formato : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(valor, formato);
            } catch (DateTimeParseException ignored) {
                // se intenta con el siguiente formato soportado
            }
        }
        throw new FilaInvalidaException("Fecha inválida: '" + valor + "'");
    }

    private <E extends Enum<E>> E parseEnum(
            Row fila, int columna, DataFormatter dataFormatter, Class<E> tipo, String etiquetaCampo
    ) {
        String valor = parseTexto(fila, columna, dataFormatter);
        if (valor == null) return null;
        return EnumLabelResolver.resolverPorLabel(tipo, valor)
                .orElseThrow(() -> new FilaInvalidaException(
                        "Valor de " + etiquetaCampo + " inválido: '" + valor + "'"));
    }

    private static class FilaInvalidaException extends RuntimeException {
        FilaInvalidaException(String message) {
            super(message);
        }
    }
}
