package com.cipolflo.server.clientes.service;
import com.cipolflo.server.clientes.domain.PagoCuota;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

public class PagoCuotaService {

    public String obtenerMesCorrespondiente(PagoCuota ultimaCuotaPaga) {
        LocalDate fecha = ultimaCuotaPaga.getFechaPago()
                .atZone(ZoneId.of("America/Montevideo"))
                .toLocalDate();

        return fecha.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-UY"));
    }
}
