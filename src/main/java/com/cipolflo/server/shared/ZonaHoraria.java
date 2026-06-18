package com.cipolflo.server.shared;

import java.time.ZoneId;

/**
 * Zona horaria del negocio. Las reservas se interpretan por día en hora local
 * de Uruguay: una reserva "hasta el 20/06" es hasta el 20/06 independientemente
 * de la hora UTC en que se haya guardado.
 */
public final class ZonaHoraria {

    private ZonaHoraria() {}

    public static final ZoneId URUGUAY = ZoneId.of("America/Montevideo");
}
