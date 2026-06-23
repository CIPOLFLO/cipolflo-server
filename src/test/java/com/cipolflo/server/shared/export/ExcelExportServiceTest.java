package com.cipolflo.server.shared.export;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelExportServiceTest {

    private final ExcelExportService service =
            new ExcelExportService(new ExportProperties(50000, 10485760));

    @Test
    void deberiaGenerarExcelConEncabezadosYFilas() throws Exception {
        byte[] archivo = service.generarExcel(
                "Finanzas",
                List.of("Tipo Movimiento", "Concepto"),
                List.of(List.of("INGRESO", "PAGO_RESERVA")),
                new int[]{6000, 6000}
        );

        assertNotNull(archivo);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            var sheet = workbook.getSheet("Finanzas");

            assertEquals("Tipo Movimiento", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Concepto", sheet.getRow(0).getCell(1).getStringCellValue());

            assertEquals("INGRESO", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("PAGO_RESERVA", sheet.getRow(1).getCell(1).getStringCellValue());
        }
    }

    @Test
    void deberiaSanitizarCeldasConRiesgoDeFormula() throws Exception {
        byte[] archivo = service.generarExcel(
                "Finanzas",
                List.of("Notas"),
                List.of(List.of("=SUMA(A1:A2)")),
                new int[]{6000}
        );

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            var sheet = workbook.getSheet("Finanzas");

            assertEquals("'=SUMA(A1:A2)", sheet.getRow(1).getCell(0).getStringCellValue());
        }
    }
    @Test
    void deberiaLanzarExceptionCuandoArchivoSuperaTamanioMaximo() {
        ExcelExportService serviceConLimiteBajo =
                new ExcelExportService(new ExportProperties(50000, 1));

        ExportacionException exception = assertThrows(
                ExportacionException.class,
                () -> serviceConLimiteBajo.generarExcel(
                        "Finanzas",
                        List.of("Concepto"),
                        List.of(List.of("PAGO_RESERVA")),
                        new int[]{6000}
                )
        );

        assertEquals(
                "El archivo generado supera el tamaño máximo permitido",
                exception.getMessage()
        );
    }
}
