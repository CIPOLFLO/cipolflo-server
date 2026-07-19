package com.cipolflo.server.integraciones.mensajeria.ai.tools;

import com.cipolflo.server.servicios.dto.ServicioReferenciaDto;
import com.cipolflo.server.servicios.dto.ServicioReservaOcupacionDto;
import com.cipolflo.server.servicios.service.IConsultaServicioSimple;
import com.cipolflo.server.servicios.service.IServicioService;
import com.cipolflo.server.shared.enums.Procedencia;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tool de solo lectura para consultar disponibilidad de servicios de la asociación.
 * No desambigua homónimos: si el nombre matchea más de un servicio, responde por todos
 * (ver regla transversal del paquete {@code ai/tools}). No lanza excepciones ante entrada
 * inválida o ambigua: devuelve un texto explicativo para que el modelo pueda repreguntar.
 */
@Component
public class DisponibilidadTools {

    private static final int MAX_COINCIDENCIAS = 10;

    private final IConsultaServicioSimple consultaServicioSimple;
    private final IServicioService servicioService;

    public DisponibilidadTools(IConsultaServicioSimple consultaServicioSimple, IServicioService servicioService) {
        this.consultaServicioSimple = consultaServicioSimple;
        this.servicioService = servicioService;
    }

    @Tool(description = "Consulta si un servicio de la asociación está disponible en un rango de fechas. "
            + "Si la asociación tiene más de un servicio con el mismo nombre, la procedencia (sede o camping) los "
            + "distingue, y se devuelve la disponibilidad de todos.")
    public String consultarDisponibilidad(
            @ToolParam(description = "Nombre del servicio, ej. 'cabaña 2', 'salón'") String nombreServicio,
            @ToolParam(description = "SEDE, CAMPING o AMBOS. Omitir si el usuario no lo aclaró.", required = false)
            Procedencia procedencia,
            @ToolParam(description = "Fecha desde, formato ISO (yyyy-MM-dd)") LocalDate desde,
            @ToolParam(description = "Fecha hasta, formato ISO (yyyy-MM-dd)") LocalDate hasta) {

        if (nombreServicio == null || nombreServicio.isBlank()) {
            return "Necesito que me indiques el nombre del servicio a consultar.";
        }
        if (desde == null || hasta == null) {
            return "Necesito que me indiques tanto la fecha desde como la fecha hasta para consultar disponibilidad.";
        }
        if (desde.isAfter(hasta)) {
            return "La fecha desde no puede ser posterior a la fecha hasta. ¿Podrías confirmarme el rango de fechas?";
        }

        List<ServicioReferenciaDto> coincidencias = consultaServicioSimple.buscarPorNombre(nombreServicio, procedencia);
        if (coincidencias.isEmpty()) {
            return "No encontré ningún servicio llamado '" + nombreServicio + "'.";
        }

        boolean hayMas = coincidencias.size() > MAX_COINCIDENCIAS;
        List<ServicioReferenciaDto> aMostrar = hayMas ? coincidencias.subList(0, MAX_COINCIDENCIAS) : coincidencias;

        String cuerpo = aMostrar.stream()
                .map(servicio -> bloqueDisponibilidad(servicio, desde, hasta))
                .collect(Collectors.joining("\n\n"));

        if (!hayMas) {
            return cuerpo;
        }
        return cuerpo + "\n\nHay más resultados de los que puedo mostrar (" + coincidencias.size()
                + " en total). Afiná la búsqueda para acotarlos.";
    }

    private String bloqueDisponibilidad(ServicioReferenciaDto servicio, LocalDate desde, LocalDate hasta) {
        String encabezado = servicio.nombre() + " (" + servicio.procedencia() + ")";

        if (!Boolean.TRUE.equals(servicio.habilitado())) {
            return encabezado + ": este servicio está deshabilitado actualmente.";
        }

        List<ServicioReservaOcupacionDto> ocupaciones = servicioService.getFechasOcupadas(servicio.id(), desde, hasta);
        if (ocupaciones.isEmpty()) {
            return encabezado + ": disponible en todo el rango consultado.";
        }

        String rangos = ocupaciones.stream()
                .map(o -> o.fechaInicio() + " a " + o.fechaFin())
                .collect(Collectors.joining(", "));
        return encabezado + ": ocupado en estas fechas -> " + rangos + ".";
    }
}
