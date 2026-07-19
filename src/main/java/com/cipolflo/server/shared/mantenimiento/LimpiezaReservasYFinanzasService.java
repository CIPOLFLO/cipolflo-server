package com.cipolflo.server.shared.mantenimiento;

import com.cipolflo.server.finanzas.repository.FinanzaRepository;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import com.cipolflo.server.shared.scheduling.ClaveConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTarea;
import com.cipolflo.server.shared.scheduling.ConfiguracionTareaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Limpia reservas viejas junto con sus finanzas asociadas, y finanzas sueltas (sin reserva)
 * vencidas. Dos reglas de retención independientes, ambas en la misma corrida:
 *
 * <ul>
 *   <li>Reservas con {@code fechaSalida} de {@code LIMPIEZA_RESERVAS_RETENCION_ANIOS} años o
 *       más: se borran junto con todas sus finanzas asociadas (Ingreso/Egreso con ese
 *       reservaId), sin mirar el estado de la reserva ni la antigüedad propia de esas
 *       finanzas — una finanza atada a una reserva no tiene vida independiente de ella.</li>
 *   <li>Finanzas sin reserva asociada ({@code reservaId} nulo) con {@code fecha} de
 *       {@code LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS} años o más: se borran
 *       independientemente.</li>
 * </ul>
 *
 * Los años de retención se leen de {@code configuracion_tarea} (no de application.properties)
 * para que se puedan ajustar sin necesidad de un deploy.
 */
@Service
public class LimpiezaReservasYFinanzasService implements ILimpiezaReservasYFinanzasService {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaReservasYFinanzasService.class);

    private final ReservaRepository reservaRepository;
    private final FinanzaRepository finanzaRepository;
    private final ConfiguracionTareaRepository configuracionTareaRepository;

    public LimpiezaReservasYFinanzasService(ReservaRepository reservaRepository,
                                            FinanzaRepository finanzaRepository,
                                            ConfiguracionTareaRepository configuracionTareaRepository) {
        this.reservaRepository = reservaRepository;
        this.finanzaRepository = finanzaRepository;
        this.configuracionTareaRepository = configuracionTareaRepository;
    }

    @Override
    @Transactional
    public String limpiarVencidos() {
        int aniosReservas = leerRetencionAnios(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS);
        int aniosFinanzasSueltas = leerRetencionAnios(ClaveConfiguracionTarea.LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS);

        LocalDate hoy = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate limiteReservas = hoy.minusYears(aniosReservas);
        LocalDate limiteFinanzasSueltas = hoy.minusYears(aniosFinanzasSueltas);

        int finanzasDeReservaBorradas = 0;
        int reservasBorradas = 0;
        List<Long> idsReservas = reservaRepository.findIdsByFechaSalidaLessThanEqual(limiteReservas);
        if (!idsReservas.isEmpty()) {
            finanzasDeReservaBorradas += finanzaRepository.deleteIngresosByReservaIdIn(idsReservas);
            finanzasDeReservaBorradas += finanzaRepository.deleteEgresosByReservaIdIn(idsReservas);
            reservasBorradas = reservaRepository.deleteByIdIn(idsReservas);
        }

        int finanzasSueltasBorradas = finanzaRepository.deleteIngresosSueltosConFechaAnteriorA(limiteFinanzasSueltas)
                + finanzaRepository.deleteEgresosSueltosConFechaAnteriorA(limiteFinanzasSueltas);

        log.info("Limpieza reservas/finanzas: {} reservas eliminadas (anteriores a {}, con {} finanzas asociadas); " +
                        "{} finanzas sueltas eliminadas (anteriores a {}).",
                reservasBorradas, limiteReservas, finanzasDeReservaBorradas,
                finanzasSueltasBorradas, limiteFinanzasSueltas);

        return ("%d reservas eliminadas (anteriores a %s, junto con %d finanzas asociadas); " +
                "%d finanzas sueltas eliminadas (anteriores a %s).").formatted(
                reservasBorradas, limiteReservas, finanzasDeReservaBorradas,
                finanzasSueltasBorradas, limiteFinanzasSueltas);
    }

    private int leerRetencionAnios(ClaveConfiguracionTarea clave) {
        ConfiguracionTarea config = configuracionTareaRepository.findById(clave)
                .orElseThrow(() -> new IllegalStateException(
                        "Falta configurar " + clave + " en configuracion_tarea; se aborta la limpieza."));
        int anios = config.valorComoEntero();
        if (anios <= 0) {
            // Guarda defensiva: una config en 0/negativo borraría todo lo que encuentre. Mejor
            // abortar y que el ejecutor lo registre como FALLIDO que purgar las tablas por un typo.
            throw new IllegalArgumentException(
                    clave + " debe ser > 0 (configurado: " + anios + "); se aborta la limpieza.");
        }
        return anios;
    }
}
