package com.cipolflo.server.clientes.service;

/**
 * Inactivación automática de socios morosos. Ver {@link InactivacionSociosService}.
 */
public interface IInactivacionSociosService {

    /**
     * Evalúa a todos los socios {@code ACTIVO} y, para los que no tienen pagada la cuota
     * correspondiente al mes en curso, registra un mes más sin pagar. Al tercer mes
     * consecutivo el socio pasa automáticamente a {@code INACTIVO}.
     *
     * @return resumen legible de lo que hizo (para el log de tareas programadas)
     */
    String inactivarSociosMorosos();
}
