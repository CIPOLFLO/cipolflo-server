package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.dto.ImportacionSociosResponseDto;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.export.ExcelExportService;
import com.cipolflo.server.shared.export.ExportProperties;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportacionSociosServiceTest {

    private static final String[] ENCABEZADO = {
            "numeroSocio", "cedula", "nombreCompleto", "fechaNacimiento", "telefono",
            "email", "metodoCobro", "pais", "departamento", "ciudad", "direccion",
            "observaciones", "categoriaSocio", "fechaIngreso", "estado"
    };

    @Mock
    private ClienteRepository clienteRepository;

    private ImportacionSociosService service;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ExcelExportService exportService = new ExcelExportService(new ExportProperties(50000, 10485760));
        service = new ImportacionSociosService(clienteRepository, validator, exportService);
    }

    private String[] filaValida(String numeroSocio, String cedula, String email) {
        return new String[]{
                numeroSocio, cedula, "Juan Pérez", "01/01/1990", "099123456",
                email, "Efectivo", "Uruguay", "Montevideo", "Montevideo", "Av. Italia 123",
                "Sin observaciones", "Socio común", "01/01/2020", "Activo"
        };
    }

    private MockMultipartFile construirArchivo(String[]... filas) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Socios");
            escribirFila(hoja, 0, ENCABEZADO);
            for (int i = 0; i < filas.length; i++) {
                escribirFila(hoja, i + 1, filas[i]);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new MockMultipartFile(
                    "file", "socios.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray()
            );
        }
    }

    private void escribirFila(Sheet hoja, int indice, String[] valores) {
        Row fila = hoja.createRow(indice);
        for (int i = 0; i < valores.length; i++) {
            if (valores[i] != null) {
                fila.createCell(i).setCellValue(valores[i]);
            }
        }
    }

    @Test
    void deberiaImportarUnaFilaValida() throws Exception {
        MockMultipartFile archivo = construirArchivo(filaValida("1", "12345678", "juan@mail.com"));

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByNumeroSocio(any())).thenReturn(false);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(1, response.getTotalFilas());
        assertEquals(1, response.getFilasImportadas());
        assertEquals(0, response.getFilasConError());
        assertTrue(response.getDetalleErrores().isEmpty());
        verify(clienteRepository).saveAndFlush(any(Socio.class));
    }

    @Test
    void noDeberiaBloquearElArchivoPorCedulaRepetidaEnBase() throws Exception {
        MockMultipartFile archivo = construirArchivo(
                filaValida("1", "12345678", "juan@mail.com"),
                filaValida("2", "87654321", "otro@mail.com")
        );

        when(clienteRepository.existsByCedula("12345678")).thenReturn(true);
        when(clienteRepository.existsByCedula("87654321")).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByNumeroSocio(any())).thenReturn(false);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(2, response.getTotalFilas());
        assertEquals(1, response.getFilasImportadas());
        assertEquals(1, response.getFilasConError());
        assertEquals(ClienteCodigoError.CEDULA_DUPLICADA.name(), response.getDetalleErrores().get(0).getCodigoError());
        assertEquals(2, response.getDetalleErrores().get(0).getNumeroFila());
        verify(clienteRepository, times(1)).saveAndFlush(any(Socio.class));
    }

    @Test
    void noDeberiaBloquearElArchivoPorEmailRepetidoEnBase() throws Exception {
        MockMultipartFile archivo = construirArchivo(
                filaValida("1", "12345678", "juan@mail.com")
        );

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase("juan@mail.com")).thenReturn(true);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(1, response.getFilasConError());
        assertEquals(ClienteCodigoError.EMAIL_DUPLICADO.name(), response.getDetalleErrores().get(0).getCodigoError());
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void noDeberiaBloquearElArchivoPorNumeroSocioRepetidoEnBase() throws Exception {
        MockMultipartFile archivo = construirArchivo(
                filaValida("1", "12345678", "juan@mail.com")
        );

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByNumeroSocio(1)).thenReturn(true);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(1, response.getFilasConError());
        assertEquals(ClienteCodigoError.NUMERO_SOCIO_DUPLICADO.name(), response.getDetalleErrores().get(0).getCodigoError());
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void deberiaDetectarCedulaRepetidaDentroDelMismoArchivo() throws Exception {
        MockMultipartFile archivo = construirArchivo(
                filaValida("1", "12345678", "juan@mail.com"),
                filaValida("2", "12345678", "otro@mail.com")
        );

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByNumeroSocio(any())).thenReturn(false);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(1, response.getFilasImportadas());
        assertEquals(1, response.getFilasConError());
        assertEquals(ClienteCodigoError.CEDULA_DUPLICADA.name(), response.getDetalleErrores().get(0).getCodigoError());
        assertEquals(3, response.getDetalleErrores().get(0).getNumeroFila());
    }

    @Test
    void deberiaReportarFilaInvalidaSinAbortarElArchivoCuandoFaltaUnCampoObligatorio() throws Exception {
        String[] filaSinNombre = filaValida("1", "12345678", "juan@mail.com");
        filaSinNombre[2] = null;

        MockMultipartFile archivo = construirArchivo(
                filaSinNombre,
                filaValida("2", "87654321", "otro@mail.com")
        );

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByNumeroSocio(any())).thenReturn(false);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(2, response.getTotalFilas());
        assertEquals(1, response.getFilasImportadas());
        assertEquals(1, response.getFilasConError());
        assertEquals("FILA_INVALIDA", response.getDetalleErrores().get(0).getCodigoError());
        assertEquals(2, response.getDetalleErrores().get(0).getNumeroFila());
    }

    @Test
    void deberiaReportarFilaInvalidaCuandoElEstadoNoMatcheaNingunaEtiqueta() throws Exception {
        String[] filaConEstadoInvalido = filaValida("1", "12345678", "juan@mail.com");
        filaConEstadoInvalido[14] = "Suspendido";

        MockMultipartFile archivo = construirArchivo(filaConEstadoInvalido);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(1, response.getFilasConError());
        assertEquals("FILA_INVALIDA", response.getDetalleErrores().get(0).getCodigoError());
        assertTrue(response.getDetalleErrores().get(0).getMotivo().contains("estado"));
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void deberiaImportarSocioInactivoConEstadoDelArchivo() throws Exception {
        String[] filaInactiva = filaValida("1", "12345678", "juan@mail.com");
        filaInactiva[14] = "Inactivo";

        MockMultipartFile archivo = construirArchivo(filaInactiva);

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByNumeroSocio(any())).thenReturn(false);

        service.importarSocios(archivo);

        verify(clienteRepository).saveAndFlush(argThat(cliente ->
                cliente instanceof Socio socio
                        && socio.getEstado().name().equals("INACTIVO")
                        && socio.getNumeroSocio().equals(1)
        ));
    }

    @Test
    void deberiaCompletarCategoriaSocioComoSocioComunCuandoNoViene() throws Exception {
        String[] filaSinCategoria = filaValida("1", "12345678", "juan@mail.com");
        filaSinCategoria[12] = null;

        MockMultipartFile archivo = construirArchivo(filaSinCategoria);

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByNumeroSocio(any())).thenReturn(false);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(1, response.getFilasImportadas());
        assertEquals(0, response.getFilasConError());
        verify(clienteRepository).saveAndFlush(argThat(cliente ->
                cliente instanceof Socio socio
                        && socio.getCategoriaSocio() == com.cipolflo.server.clientes.domain.enums.CategoriaSocio.SOCIO_COMUN
        ));
    }

    @Test
    void deberiaLanzarExcepcionCuandoElArchivoNoTieneFilasDeDatos() throws Exception {
        MockMultipartFile archivo = construirArchivo();

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> service.importarSocios(archivo)
        );

        assertEquals(ClienteCodigoError.ARCHIVO_IMPORTACION_INVALIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoElEncabezadoNoTieneLasColumnasEsperadas() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Socios");
            escribirFila(hoja, 0, new String[]{"numeroSocio", "cedula"});
            escribirFila(hoja, 1, new String[]{"1", "12345678"});
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            MockMultipartFile archivo = new MockMultipartFile(
                    "file", "socios.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray()
            );

            ClienteValidacionException ex = assertThrows(
                    ClienteValidacionException.class,
                    () -> service.importarSocios(archivo)
            );

            assertEquals(ClienteCodigoError.ARCHIVO_IMPORTACION_INVALIDO.name(), ex.getCodigo());
        }
    }

    @Test
    void deberiaGenerarPlantillaConEncabezadoDe15ColumnasYFilasDeEjemplo() throws Exception {
        ArchivoExportado plantilla = service.generarPlantilla();

        assertNotNull(plantilla.getContenido());
        assertTrue(plantilla.getNombre().startsWith("plantilla_importacion_socios"));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(plantilla.getContenido()))) {
            Sheet hoja = workbook.getSheet("Socios");

            assertEquals(15, hoja.getRow(0).getLastCellNum());
            assertEquals("numeroSocio", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("estado", hoja.getRow(0).getCell(14).getStringCellValue());

            assertEquals(2, hoja.getLastRowNum());
            assertEquals("Juan Pérez", hoja.getRow(1).getCell(2).getStringCellValue());
        }
    }

    @Test
    void laPlantillaGeneradaDeberiaImportarseSinErrores() throws Exception {
        ArchivoExportado plantilla = service.generarPlantilla();
        MockMultipartFile archivo = new MockMultipartFile(
                "file", "plantilla.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                plantilla.getContenido()
        );

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByMailIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByNumeroSocio(any())).thenReturn(false);

        ImportacionSociosResponseDto response = service.importarSocios(archivo);

        assertEquals(2, response.getTotalFilas());
        assertEquals(2, response.getFilasImportadas());
        assertTrue(response.getDetalleErrores().isEmpty());
    }

    @Test
    void deberiaLanzarExcepcionCuandoElArchivoNoEsUnExcelValido() {
        MockMultipartFile archivo = new MockMultipartFile(
                "file", "socios.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "esto no es un excel".getBytes()
        );

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> service.importarSocios(archivo)
        );

        assertEquals(ClienteCodigoError.ARCHIVO_IMPORTACION_INVALIDO.name(), ex.getCodigo());
    }
}
