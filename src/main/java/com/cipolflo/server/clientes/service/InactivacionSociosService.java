package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.ClienteSpecification;
import com.cipolflo.server.clientes.repository.PagoCuotaRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Corre una vez por mes (ver {@code InactivacionSociosScheduler}) y detecta socios morosos.
 * Para cada socio {@code ACTIVO}, recalcula desde cero cuántos meses completos adeuda
 * comparando el primer período pendiente (según {@code pago_cuota}) contra el mes en curso.
 * El mes en curso nunca cuenta como adeudado (recién empieza, todavía no venció). Al llegar a
 * 3 meses adeudados, el socio pasa a {@code INACTIVO} vía {@link Socio#pasarAInactivoPorMorosidad()}.
 *
 * Al recalcularse entero en cada corrida (no acumula un contador persistido), es idempotente:
 * un socio que se puso al día simplemente da 0 en la próxima corrida, y una corrida salteada
 * no descuadra el resultado de la siguiente.
 */
@Service
public class InactivacionSociosService implements IInactivacionSociosService {

    private static final Logger log = LoggerFactory.getLogger(InactivacionSociosService.class);
    private static final int MESES_PARA_INACTIVAR = 3;

    private final ClienteRepository clienteRepository;
    private final PagoCuotaRepository pagoCuotaRepository;

    public InactivacionSociosService(ClienteRepository clienteRepository,
                                     PagoCuotaRepository pagoCuotaRepository) {
        this.clienteRepository = clienteRepository;
        this.pagoCuotaRepository = pagoCuotaRepository;
    }

    @Override
    @Transactional
    public String inactivarSociosMorosos() {
        YearMonth mesActual = YearMonth.now(ZonaHoraria.URUGUAY);

        List<Socio> sociosActivos = clienteRepository.findAll(ClienteSpecification.conEstado(EstadoSocio.ACTIVO))
                .stream()
                .filter(Socio.class::isInstance)
                .map(Socio.class::cast)
                .toList();

        int inactivados = 0;

        for (Socio socio : sociosActivos) {
            int mesesAdeudados = calcularMesesAdeudados(socio, mesActual);
            if (mesesAdeudados >= MESES_PARA_INACTIVAR) {
                socio.pasarAInactivoPorMorosidad();
                clienteRepository.save(socio);
                inactivados++;
            }
        }

        log.info("Inactivación de socios: {} evaluados, {} pasados a INACTIVO.",
                sociosActivos.size(), inactivados);

        return "%d socios activos evaluados, %d pasados a INACTIVO."
                .formatted(sociosActivos.size(), inactivados);
    }

    // El mes en curso no cuenta: recién empieza, todavía no venció. Solo se cuentan los
    // meses completos entre el primer período pendiente (inclusive) y el mes en curso
    // (exclusive).
    private int calcularMesesAdeudados(Socio socio, YearMonth mesActual) {
        YearMonth primerPeriodoPendiente = calcularPrimerPeriodoPendiente(socio);
        return (int) Math.max(0, ChronoUnit.MONTHS.between(primerPeriodoPendiente, mesActual));
    }

    // Mismo cálculo que PagoCuotaService.calcularPrimerPeriodoPendiente: por invariante no
    // hay huecos, el primer período pendiente es el mes siguiente al último pagado, o
    // fechaIngreso si nunca pagó.
    private YearMonth calcularPrimerPeriodoPendiente(Socio socio) {
        return pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(socio.getId())
                .map(pago -> YearMonth.of(pago.getAnio(), pago.getMes()).plusMonths(1))
                .orElseGet(() -> YearMonth.from(socio.getFechaIngreso()));
    }
}
