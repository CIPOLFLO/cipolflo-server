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
import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import com.cipolflo.server.shared.ZonaHoraria;
import com.cipolflo.server.shared.enums.Procedencia;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cipolflo.server.clientes.helper.MetodoCobroFormaPagoHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

@Service
public class PagoCuotaService implements IPagoCuotaService {

    private final PagoCuotaRepository pagoCuotaRepository;
    private final ClienteRepository clienteRepository;
    private final IFinanzaService finanzaService;
    private static final Locale LOCALE = Locale.forLanguageTag("es-UY");

    public PagoCuotaService(
            PagoCuotaRepository pagoCuotaRepository,
            ClienteRepository clienteRepository,
            IFinanzaService finanzaService
    ) {
        this.pagoCuotaRepository = pagoCuotaRepository;
        this.clienteRepository = clienteRepository;
        this.finanzaService = finanzaService;
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
    @Transactional
    public List<PagoCuotaResponseDto> registrarPago(Long socioId, RegistroPagoCuotaRequestDto request) {
        Socio socio = validarSocio(socioId);
        YearMonth primerPeriodoPendiente = calcularPrimerPeriodoPendiente(socio);
        BigDecimal importePorMes = request.importeTotal()
                .divide(BigDecimal.valueOf(request.cantidadCuotas()), 2, RoundingMode.HALF_UP);
        Instant fechaPago = request.fechaPago()
                .atStartOfDay(ZonaHoraria.URUGUAY)
                .toInstant();
        List<PagoCuota> pagos = IntStream.range(0, request.cantidadCuotas())
                .mapToObj(i -> {
                    YearMonth periodo = primerPeriodoPendiente.plusMonths(i);
                    return PagoCuota.crear(
                            socioId,
                            periodo.getYear(),
                            periodo.getMonthValue(),
                            fechaPago,
                            importePorMes,
                            request.metodoCobro(),
                            request.observaciones()
                    );
                })
                .toList();
        try {
            List<PagoCuota> pagosGuardados = pagoCuotaRepository.saveAll(pagos);

            pagosGuardados.forEach(pago -> {
                FinanzaCrearRequestDto dtoFinanza = new FinanzaCrearRequestDto();
                dtoFinanza.setTipoMovimiento(TipoMovimiento.INGRESO);
                dtoFinanza.setProcedencia(Procedencia.SEDE);
                dtoFinanza.setConcepto(Concepto.PAGO_CUOTA);
                dtoFinanza.setFecha(LocalDate.ofInstant(pago.getFechaPago(), ZonaHoraria.URUGUAY));
                dtoFinanza.setImporte(pago.getImporte());
                dtoFinanza.setFormaPago(MetodoCobroFormaPagoHelper.toFormaPago(request.metodoCobro()));
                dtoFinanza.setNotas(pago.getObservaciones());
                dtoFinanza.setPagoCuotaId(pago.getId());

                finanzaService.registrarPagoCuota(dtoFinanza);
            });

            return pagosGuardados.stream()
                    .map(this::toPagoCuotaResponseDto)
                    .toList();
        } catch (DataIntegrityViolationException e) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.SOLICITUD_INVALIDA.name(),
                    "La cuota del período ya fue registrada"
            );
        }
    }

    // El mes en curso no cuenta como adeudado (recién empieza, todavía no venció). Mismo
    // cálculo que InactivacionSociosService.calcularMesesAdeudados.
    @Override
    public int calcularMesesAdeudados(Socio socio) {
        YearMonth mesActual = YearMonth.now(ZonaHoraria.URUGUAY);
        YearMonth primerPeriodoPendiente = calcularPrimerPeriodoPendiente(socio);
        return (int) Math.max(0, ChronoUnit.MONTHS.between(primerPeriodoPendiente, mesActual));
    }

    private Socio validarSocio(Long socioId) {
        Cliente cliente = clienteRepository.findById(socioId)
                .orElseThrow(() -> new SocioNotFoundException(socioId));
        if (!(cliente instanceof Socio socio)) {
            throw new SocioNotFoundException(socioId);
        }
        return socio;
    }

    // Por invariante no hay huecos: el primer período pendiente es siempre el mes
    // siguiente al último pagado, o fechaIngreso si nunca se pagó.
    private YearMonth calcularPrimerPeriodoPendiente(Socio socio) {
        return pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(socio.getId())
                .map(pago -> YearMonth.of(pago.getAnio(), pago.getMes()).plusMonths(1))
                .orElseGet(() -> YearMonth.from(socio.getFechaIngreso()));
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

    private String nombreMes(YearMonth periodo) {
        return periodo.getMonth().getDisplayName(TextStyle.FULL, LOCALE);
    }

    private String descripcion(YearMonth periodo) {
        String nombre = nombreMes(periodo);
        return nombre.substring(0, 1).toUpperCase(LOCALE) + nombre.substring(1) + " " + periodo.getYear();
    }
}
