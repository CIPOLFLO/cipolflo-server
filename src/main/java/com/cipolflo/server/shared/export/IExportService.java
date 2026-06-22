package com.cipolflo.server.shared.export;
import java.util.List;
public interface IExportService {
    byte[] generarExcel(String nombreHoja,List<String> encabezados, List<List<String>> filas,int[]anchosColumnas);
}
