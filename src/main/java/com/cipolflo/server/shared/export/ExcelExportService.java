package com.cipolflo.server.shared.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService implements IExportService {

    @Override
    public byte[] generarExcel(String nombreHoja, List<String> encabezados,
                               List<List<String>> filas, int[] anchosColumnas) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        try {
            SXSSFSheet sheet = workbook.createSheet(nombreHoja);

            for (int i = 0; i < anchosColumnas.length; i++) {
                sheet.setColumnWidth(i, anchosColumnas[i]);
            }

            SXSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < encabezados.size(); i++) {
                headerRow.createCell(i).setCellValue(encabezados.get(i));
            }

            for (int i = 0; i < filas.size(); i++) {
                SXSSFRow dataRow = sheet.createRow(i + 1);
                List<String> fila = filas.get(i);
                for (int j = 0; j < fila.size(); j++) {
                    Cell cell = dataRow.createCell(j);
                    String valor = fila.get(j);
                    cell.setCellValue(valor != null ? FormulaSanitizer.sanitizar(valor) : "");
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel", e);
        } finally {
            workbook.dispose();
        }
    }
}