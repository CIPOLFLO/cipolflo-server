package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.service.IConsultaClienteParaCosto;
import com.cipolflo.server.reservas.dto.CalculoCostoRequestDto;
import com.cipolflo.server.reservas.dto.CalculoCostoResponseDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.CalculoCostoValidator;
import com.cipolflo.server.servicios.costo.*;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.service.IConsultaServicioParaCosto;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculoCostoServiceTest {

    private static final Long SERVICIO_ID = 1L;
    private static final Long CLIENTE_ID = 10L;

    @Mock
    private IConsultaServicioParaCosto consultaServicio;

    @Mock
    private IConsultaClienteParaCosto consultaCliente;

    @Mock
    private ResolutorTarifaServicio resolutorTarifaServicio;

    @Mock
    private Particular particular;

    @Mock
    private Socio socio;

    @Mock
    private TarifaServicio tarifa;

    private FabricaEstrategia fabricaEstrategia;
    private CalculoCostoValidator validator;
    private CalculoCostoService service;

    @BeforeEach
    void setUp() {
        fabricaEstrategia = new FabricaEstrategia(
                new EstrategiaCostoPorDia(),
                new EstrategiaCostoPorHora(),
                new EstrategiaCostoPorUnidad(),
                new EstrategiaCostoPorPersona(),
                new EstrategiaCostoPorDiaPorPersona()
        );

        validator = new CalculoCostoValidator();

        service = new CalculoCostoService(
                consultaServicio,
                consultaCliente,
                fabricaEstrategia,
                resolutorTarifaServicio,
                validator
        );
    }

    private Servicio servicio(
            ModalidadPrecio modalidad,
            Integer capacidad,
            BigDecimal costoExtra
    ) {
        Servicio servicio = new Servicio();

        ReflectionTestUtils.setField(servicio, "id", SERVICIO_ID);

        servicio.setNombre("test");
        servicio.setProcedencia(Procedencia.CAMPING);
        servicio.setModalidadPrecio(modalidad);
        servicio.setCapacidad(capacidad);
        servicio.setCostoPersonaExtra(costoExtra);
        servicio.setHabilitado(true);

        return servicio;
    }

    private CalculoCostoRequestDto request(
            Long servicioId,
            Long clienteId,
            LocalDate inicio,
            LocalDate fin,
            LocalTime horaInicio,
            LocalTime horaFin,
            Integer cantidadTotal,
            Integer cantidad,
            Integer cantidadMenores
    ) {
        CalculoCostoRequestDto dto = new CalculoCostoRequestDto();

        ReflectionTestUtils.setField(dto, "servicioId", servicioId);
        ReflectionTestUtils.setField(dto, "clienteId", clienteId);
        ReflectionTestUtils.setField(dto, "fechaInicio", inicio);
        ReflectionTestUtils.setField(dto, "fechaFin", fin);
        ReflectionTestUtils.setField(dto, "horaInicio", horaInicio);
        ReflectionTestUtils.setField(dto, "horaFin", horaFin);
        ReflectionTestUtils.setField(dto, "cantidadTotal", cantidadTotal);
        ReflectionTestUtils.setField(dto, "cantidad", cantidad);
        ReflectionTestUtils.setField(dto, "cantidadMenores", cantidadMenores);

        return dto;
    }

    private void configurarParticular(
            Servicio servicio,
            ModalidadPrecio modalidad
    ) {
        when(consultaServicio.obtenerServicio(SERVICIO_ID))
                .thenReturn(servicio);

        when(consultaCliente.obtenerCliente(CLIENTE_ID))
                .thenReturn(particular);

        when(consultaServicio.obtenerTarifas(SERVICIO_ID))
                .thenReturn(List.of(tarifa));

        when(resolutorTarifaServicio.resolver(
                anyList(),
                eq(TipoClienteTarifa.PARTICULAR),
                isNull()
        )).thenReturn(tarifa);

        when(tarifa.getModalidadPrecio())
                .thenReturn(modalidad);
    }

    private void configurarSocio(
            Servicio servicio,
            ModalidadPrecio modalidad
    ) {
        when(consultaServicio.obtenerServicio(SERVICIO_ID))
                .thenReturn(servicio);

        /*
         * Se utiliza una categoría válida del enum porque el mapper necesita
         * transformar CategoriaSocio en TipoClienteTarifa.
         */
        when(socio.getCategoriaSocio())
                .thenReturn(CategoriaSocio.values()[0]);

        when(socio.calcularAntiguedadEnAnios(any(LocalDate.class)))
                .thenReturn(5);

        when(consultaCliente.obtenerCliente(CLIENTE_ID))
                .thenReturn(socio);

        when(consultaServicio.obtenerTarifas(SERVICIO_ID))
                .thenReturn(List.of(tarifa));

        when(resolutorTarifaServicio.resolver(
                anyList(),
                any(TipoClienteTarifa.class),
                eq(5)
        )).thenReturn(tarifa);

        when(tarifa.getModalidadPrecio())
                .thenReturn(modalidad);
    }

    @Test
    void servicioNoEncontrado_lanzaServicioNotFoundException() {
        when(consultaServicio.obtenerServicio(99L))
                .thenThrow(new ServicioNotFoundException(99L));

        CalculoCostoRequestDto dto = request(
                99L,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(
                ServicioNotFoundException.class,
                () -> service.calcularCosto(dto)
        );
    }

    @Test
    void clienteParticular_usaTarifaParticular() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_DIA,
                null,
                null
        );

        configurarParticular(servicio, ModalidadPrecio.POR_DIA);

        when(tarifa.getPrecio())
                .thenReturn(new BigDecimal("1000"));

        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                null,
                null,
                null,
                null,
                null
        );

        CalculoCostoResponseDto resultado =
                service.calcularCosto(dto);

        assertEquals(
                new BigDecimal("1000"),
                resultado.costoTotal()
        );

        verify(resolutorTarifaServicio).resolver(
                anyList(),
                eq(TipoClienteTarifa.PARTICULAR),
                isNull()
        );
    }

    @Test
    void clienteSocio_usaTarifaResueltaParaSocio() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_DIA,
                null,
                null
        );

        configurarSocio(servicio, ModalidadPrecio.POR_DIA);

        when(tarifa.getPrecio())
                .thenReturn(new BigDecimal("700"));

        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                null,
                null,
                null,
                null,
                null
        );

        CalculoCostoResponseDto resultado =
                service.calcularCosto(dto);

        assertEquals(
                new BigDecimal("2100"),
                resultado.costoTotal()
        );

        verify(resolutorTarifaServicio).resolver(
                anyList(),
                any(TipoClienteTarifa.class),
                eq(5)
        );
    }

    @Test
    void modalidadPorDia_calculaTresDias() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_DIA,
                null,
                null
        );

        configurarParticular(servicio, ModalidadPrecio.POR_DIA);

        when(tarifa.getPrecio())
                .thenReturn(new BigDecimal("1000"));

        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                null,
                null,
                null,
                null,
                null
        );

        CalculoCostoResponseDto resultado =
                service.calcularCosto(dto);

        assertEquals(
                new BigDecimal("3000"),
                resultado.costoTotal()
        );
    }

    @Test
    void modalidadPorHora_calculaHoras() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_HORA,
                null,
                null
        );

        configurarParticular(servicio, ModalidadPrecio.POR_HORA);

        when(tarifa.getPrecio())
                .thenReturn(new BigDecimal("200"));

        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                null,
                null,
                null
        );

        CalculoCostoResponseDto resultado =
                service.calcularCosto(dto);

        assertEquals(
                new BigDecimal("800"),
                resultado.costoTotal()
        );
    }

    @Test
    void modalidadPorUnidad_calculaCantidad() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_UNIDAD,
                null,
                null
        );

        configurarParticular(servicio, ModalidadPrecio.POR_UNIDAD);

        when(tarifa.getPrecio())
                .thenReturn(new BigDecimal("500"));

        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                null,
                null,
                null,
                5,
                null
        );

        CalculoCostoResponseDto resultado =
                service.calcularCosto(dto);

        assertEquals(
                new BigDecimal("2500"),
                resultado.costoTotal()
        );
    }

    @Test
    void modalidadPorPersona_conExcedente() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_PERSONA,
                4,
                new BigDecimal("300")
        );

        configurarParticular(servicio, ModalidadPrecio.POR_PERSONA);

        when(tarifa.getPrecio())
                .thenReturn(new BigDecimal("2000"));

        /*
         * 7 personas, 1 menor:
         * adultos equivalentes = 6
         * excedente = 2
         * 2000 + (2 × 300) = 2600
         */
        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                null,
                null,
                7,
                null,
                1
        );

        CalculoCostoResponseDto resultado =
                service.calcularCosto(dto);

        assertEquals(
                new BigDecimal("2600"),
                resultado.costoTotal()
        );
    }

    @Test
    void modalidadPorDiaPorPersona_multiplicaPorDias() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_DIA_POR_PERSONA,
                4,
                new BigDecimal("300")
        );

        configurarParticular(
                servicio,
                ModalidadPrecio.POR_DIA_POR_PERSONA
        );

        when(tarifa.getPrecio())
                .thenReturn(new BigDecimal("2000"));

        /*
         * 3 personas, sin excedente:
         * 2000 × 2 días = 4000
         */
        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 2),
                null,
                null,
                3,
                null,
                0
        );

        CalculoCostoResponseDto resultado =
                service.calcularCosto(dto);

        assertEquals(
                new BigDecimal("4000"),
                resultado.costoTotal()
        );
    }

    @Test
    void fechaFinAnteriorAInicio_lanzaValidacionException() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_DIA,
                null,
                null
        );

        configurarParticular(servicio, ModalidadPrecio.POR_DIA);

        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 1),
                null,
                null,
                null,
                null,
                null
        );

        ReservaValidacionException excepcion =
                assertThrows(
                        ReservaValidacionException.class,
                        () -> service.calcularCosto(dto)
                );

        assertEquals(
                ReservaCodigoError.FECHA_FIN_ANTERIOR_A_INICIO.name(),
                excepcion.getCodigo()
        );
    }

    @Test
    void porHora_sinHoras_lanzaValidacionException() {
        Servicio servicio = servicio(
                ModalidadPrecio.POR_HORA,
                null,
                null
        );

        configurarParticular(servicio, ModalidadPrecio.POR_HORA);

        CalculoCostoRequestDto dto = request(
                SERVICIO_ID,
                CLIENTE_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                null,
                null,
                null,
                null,
                null
        );

        ReservaValidacionException excepcion =
                assertThrows(
                        ReservaValidacionException.class,
                        () -> service.calcularCosto(dto)
                );

        assertEquals(
                ReservaCodigoError.HORA_REQUERIDA_PARA_SERVICIO_POR_HORA.name(),
                excepcion.getCodigo()
        );
    }
}