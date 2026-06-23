package com.cipolflo.server.shared.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class ExcelExportService implements IExportService {

    private final ExportProperties exportProperties;

    public ExcelExportService(ExportProperties exportProperties) {
        this.exportProperties = exportProperties;
    }

    @Override
    public byte[] generarExcel(
            String nombreHoja,
            List<String> encabezados,
            List<List<String>> filas,
            int[] anchosColumnas
    ) {
        try (
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            SXSSFSheet sheet = workbook.createSheet(nombreHoja);

            escribirEncabezados(sheet, encabezados);
            escribirFilas(sheet, filas);
            aplicarAnchos(sheet, anchosColumnas);

            workbook.write(outputStream);

            byte[] archivo = outputStream.toByteArray();

            if (archivo.length > exportProperties.maxBytes()) {
                throw new ExportacionException(
                        "El archivo generado supera el tamaño máximo permitido"
                );
            }

            workbook.dispose();

            return archivo;
        } catch (IOException e) {
            throw new UncheckedIOException("Error al generar archivo Excel", e);
        }
    }

    private void escribirEncabezados(SXSSFSheet sheet, List<String> encabezados) {
        Row row = sheet.createRow(0);

        for (int i = 0; i < encabezados.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(encabezados.get(i));
        }
    }

    private void escribirFilas(SXSSFSheet sheet, List<List<String>> filas) {
        for (int i = 0; i < filas.size(); i++) {
            Row row = sheet.createRow(i + 1);
            List<String> fila = filas.get(i);

            for (int j = 0; j < fila.size(); j++) {
                Cell cell = row.createCell(j);
                String valor = FormulaSanitizer.sanitizar(fila.get(j));
                cell.setCellValue(valor);
            }
        }
    }

    private void aplicarAnchos(SXSSFSheet sheet, int[] anchosColumnas) {
        for (int i = 0; i < anchosColumnas.length; i++) {
            sheet.setColumnWidth(i, anchosColumnas[i]);
        }
    }
}