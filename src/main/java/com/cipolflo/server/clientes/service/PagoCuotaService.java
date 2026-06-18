package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.PagoCuota;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.dto.PagoCuotaResponseDto;
import com.cipolflo.server.clientes.dto.PeriodoCuotaDto;
import com.cipolflo.server.clientes.dto.RegistroPagoCuotaRequestDto;
import com.cipolflo.server.clientes.dto.UltimaCuotaDto;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.PagoCuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
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
public class PagoCuotaService implements IPagoCuotaService {

    private final PagoCuotaRepository pagoCuotaRepository;
    private final ClienteRepository clienteRepository;

    private static final Locale LOCALE = Locale.forLanguageTag("es-UY");

    public PagoCuotaService(
            PagoCuotaRepository pagoCuotaRepository,
            ClienteRepository clienteRepository
    ) {
        this.pagoCuotaRepository = pagoCuotaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UltimaCuotaDto calcularUltimaCuotaPaga(Long socioId) {
        validarSocio(socioId);

        return pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(socioId)
                .map(pago -> toUltimaCuotaDto(YearMonth.of(pago.getAnio(), pago.getMes())))
                .orElse(null);
    }

    @Override
    public List<PeriodoCuotaDto> calcularPeriodosCubiertos(Long socioId, Integer cantidadCuotas) {
        Socio socio = validarSocio(socioId);
        YearMonth primerPeriodoPendiente = calcularPrimerPeriodoPendiente(socio);

        return IntStream.range(0, cantidadCuotas)
                .mapToObj(primerPeriodoPendiente::plusMonths)
                .map(this::toPeriodoCuotaDto)
                .toList();
    }

    @Override
    public int calcularMesesPendientes(Long socioId) {
        Socio socio = validarSocio(socioId);
        return calcularPeriodosPendientes(socio).size();
    }

    private Socio validarSocio(Long socioId) {
        Cliente cliente = clienteRepository.findById(socioId)
                .orElseThrow(() -> new SocioNotFoundException(socioId));

        if (!(cliente instanceof Socio socio)) {
            throw new SocioNotFoundException(socioId);
        }

        return socio;
    }

    @Override
    @Transactional
    public List<PagoCuotaResponseDto> registrarPago(
            Long socioId,
            RegistroPagoCuotaRequestDto request
    ) {
        Socio socio = validarSocio(socioId);
        List<YearMonth> periodos = generarPeriodosCubiertos(
                socio,
                request.cantidadCuotas()
        );

        validarPeriodosNoPagos(socioId, periodos);
        BigDecimal importePorMes = request.importeTotal()
                .divide(
                        BigDecimal.valueOf(request.cantidadCuotas()),
                        2,
                        RoundingMode.HALF_UP
                );
        Instant fechaPago = request.fechaPago()
                .atStartOfDay(ZoneId.systemDefault())
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
        List<PagoCuota> pagosGuardados = pagoCuotaRepository.saveAll(pagos);
        return pagosGuardados.stream()
                .map(this::toPagoCuotaResponseDto)
                .toList();
    }

    private YearMonth calcularPrimerPeriodoPendiente(Socio socio) {
        return calcularPeriodosPendientes(socio)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PagoCuota ultimaCuotaPaga = pagoCuotaRepository
                            .findTopBySocioIdOrderByAnioDescMesDesc(socio.getId())
                            .orElse(null);

                    if (ultimaCuotaPaga == null) {
                        return YearMonth.from(socio.getFechaIngreso());
                    }

                    return YearMonth.of(
                            ultimaCuotaPaga.getAnio(),
                            ultimaCuotaPaga.getMes()
                    ).plusMonths(1);
                });
    }

    private List<YearMonth> calcularPeriodosPendientes(Socio socio) {
        YearMonth desde = YearMonth.from(socio.getFechaIngreso());
        YearMonth hasta = YearMonth.now();

        Set<YearMonth> periodosPagos = pagoCuotaRepository.findBySocioId(socio.getId())
                .stream()
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

    private UltimaCuotaDto toUltimaCuotaDto(YearMonth periodo) {
        return new UltimaCuotaDto(
                periodo.getYear(),
                periodo.getMonthValue(),
                nombreMes(periodo),
                descripcion(periodo)
        );
    }

    private PeriodoCuotaDto toPeriodoCuotaDto(YearMonth periodo) {
        return new PeriodoCuotaDto(
                periodo.getYear(),
                periodo.getMonthValue(),
                nombreMes(periodo),
                descripcion(periodo)
        );
    }

    private String nombreMes(YearMonth periodo) {
        return periodo.getMonth()
                .getDisplayName(TextStyle.FULL, LOCALE);
    }

    private String descripcion(YearMonth periodo) {
        String nombreMes = nombreMes(periodo);
        String nombreCapitalizado = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);

        return nombreCapitalizado + " " + periodo.getYear();
    }
    private List<YearMonth> generarPeriodosCubiertos(
            Socio socio,
            Integer cantidadCuotas
    ) {
        YearMonth primerPeriodoPendiente = calcularPrimerPeriodoPendiente(socio);

        return IntStream.range(0, cantidadCuotas)
                .mapToObj(primerPeriodoPendiente::plusMonths)
                .toList();
    }

    private PagoCuotaResponseDto toPagoCuotaResponseDto(PagoCuota pago) {
        YearMonth periodo = YearMonth.of(pago.getAnio(), pago.getMes());

        return new PagoCuotaResponseDto(
                pago.getId(),
                pago.getSocioId(),
                pago.getAnio(),
                pago.getMes(),
                nombreMes(periodo),
                descripcion(periodo),
                pago.getFechaPago(),
                pago.getImporte(),
                pago.getMetodoCobro()
        );
    }
    private void validarPeriodosNoPagos(Long socioId, List<YearMonth> periodos) {
        for (YearMonth periodo : periodos) {
            if (pagoCuotaRepository.existsBySocioIdAndAnioAndMes(
                    socioId,
                    periodo.getYear(),
                    periodo.getMonthValue()
            )) {
                throw new ClienteValidacionException(
                        ClienteCodigoError.SOLICITUD_INVALIDA.name(),
                        "La cuota del período ya fue registrada"
                );
            }
        }
    }
}


