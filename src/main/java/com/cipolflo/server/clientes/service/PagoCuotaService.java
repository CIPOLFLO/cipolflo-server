package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.PagoCuota;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.dto.CuotaPendienteDto;
import com.cipolflo.server.clientes.dto.RegistroPagoCuotaRequestDto;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.PagoCuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PagoCuotaService {

    private static final ZoneId ZONA = ZoneId.of("America/Montevideo");
    private static final Locale LOCALE = Locale.forLanguageTag("es-UY");

    private final PagoCuotaRepository pagoCuotaRepository;
    private final ClienteRepository clienteRepository;

    public PagoCuotaService(
            PagoCuotaRepository pagoCuotaRepository,
            ClienteRepository clienteRepository
    ) {
        this.pagoCuotaRepository = pagoCuotaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public void registrarPago(Long socioId, RegistroPagoCuotaRequestDto request) {
        Cliente cliente = clienteRepository.findById(socioId)
                .orElseThrow(() -> new SocioNotFoundException(socioId));

        if (!(cliente instanceof Socio socio)) {
            throw new SocioNotFoundException(socioId);
        }

        List<YearMonth> periodos = generarPeriodos(
                request.anioDesde(),
                request.mesDesde(),
                request.cantidadMeses()
        );

        validarPeriodosNoPagos(socioId, periodos);

        BigDecimal importePorMes = request.importeTotal()
                .divide(BigDecimal.valueOf(request.cantidadMeses()), 2, RoundingMode.HALF_UP);

        Instant fechaPago = request.fechaPago()
                .atStartOfDay(ZONA)
                .toInstant();

        List<PagoCuota> pagos = periodos.stream()
                .map(periodo -> PagoCuota.crear(
                        socioId,
                        periodo.getYear(),
                        periodo.getMonthValue(),
                        fechaPago,
                        importePorMes,
                        request.metodoCobro(),
                        request.observaciones()
                ))
                .toList();

        pagoCuotaRepository.saveAll(pagos);

        socio.setFechaUltimoPago(request.fechaPago());

        int mesesSinPagar = calcularMesesPendientes(socio);
        socio.actualizarMesesSinPagar(mesesSinPagar);

        clienteRepository.save(socio);
    }

    public List<CuotaPendienteDto> obtenerCuotasPendientes(Long socioId) {
        Cliente cliente = clienteRepository.findById(socioId)
                .orElseThrow(() -> new SocioNotFoundException(socioId));

        if (!(cliente instanceof Socio socio)) {
            throw new SocioNotFoundException(socioId);
        }

        return calcularPeriodosPendientes(socio).stream()
                .map(periodo -> new CuotaPendienteDto(
                        periodo.getYear(),
                        periodo.getMonthValue(),
                        nombreMes(periodo)
                ))
                .toList();
    }

    public int calcularMesesPendientes(Socio socio) {
        return calcularPeriodosPendientes(socio).size();
    }

    private List<YearMonth> calcularPeriodosPendientes(Socio socio) {
        YearMonth desde = YearMonth.from(socio.getFechaIngreso());
        YearMonth hasta = YearMonth.now(ZONA);

        Set<YearMonth> periodosPagos = pagoCuotaRepository.findBySocioId(socio.getId()).stream()
                .map(pago -> YearMonth.of(pago.getAnio(), pago.getMes()))
                .collect(Collectors.toSet());

        List<YearMonth> pendientes = new ArrayList<>();

        YearMonth actual = desde;
        while (!actual.isAfter(hasta)) {
            if (!periodosPagos.contains(actual)) {
                pendientes.add(actual);
            }
            actual = actual.plusMonths(1);
        }

        return pendientes;
    }

    private void validarPeriodosNoPagos(Long socioId, List<YearMonth> periodos) {
        for (YearMonth periodo : periodos) {
            if (pagoCuotaRepository.existsBySocioIdAndAnioAndMes(
                    socioId,
                    periodo.getYear(),
                    periodo.getMonthValue()
            )) {
                throw new ClienteValidacionException(
                        "CUOTA_YA_REGISTRADA",
                        "La cuota del período ya fue registrada"
                );
            }
        }
    }

    private List<YearMonth> generarPeriodos(Integer anioDesde, Integer mesDesde, Integer cantidadMeses) {
        YearMonth inicio = YearMonth.of(anioDesde, mesDesde);

        return IntStream.range(0, cantidadMeses)
                .mapToObj(inicio::plusMonths)
                .toList();
    }

    private String nombreMes(YearMonth periodo) {
        return periodo.getMonth()
                .getDisplayName(TextStyle.FULL, LOCALE);
    }
}