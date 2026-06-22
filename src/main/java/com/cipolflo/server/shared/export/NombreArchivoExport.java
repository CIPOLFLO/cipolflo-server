package com.cipolflo.server.shared.export;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class NombreArchivoExport {
   private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm");
   
    private NombreArchivoExport(){}
    public static String generar(String prefijo){
        String timestamp = LocalDateTime.now(ZoneId.systemDefault()).format(FORMATO);
        return prefijo + "_" + timestamp + ".xlsx";
    }
}
