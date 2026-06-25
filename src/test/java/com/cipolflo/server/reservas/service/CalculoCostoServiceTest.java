package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.reservas.dto.CalculoCostoRequestDto;
import com.cipolflo.server.reservas.dto.CalculoCostoResponseDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.validators.CalculoCostoValidator;
import com.cipolflo.server.servicios.costo.*;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculoCostoServiceTest {

    @Mock
    private IConsultaServicioParaCosto consultaServicio;

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
        service = new CalculoCostoService(consultaServicio, fabricaEstrategia, validator);
    }

    private Servicio servicio(ModalidadPrecio modalidad, BigDecimal precioParticular,
                               BigDecimal precioSocio, Integer capacidad, BigDecimal costoExtra) {
        Servicio s = new Servicio();
        ReflectionTestUtils.setField(s, "id", 1L);
        s.setNombre("test");
        s.setProcedencia(Procedencia.CAMPING);
        s.setModalidadPrecio(modalidad);
        s.setPrecioParticular(precioParticular);
        s.setPrecioSocio(precioSocio);
        s.setCapacidad(capacidad);
        s.setCostoPersonaExtra(costoExtra);
        s.setHabilitado(true);
        return s;
    }

    private CalculoCostoRequestDto request(Long servicioId, LocalDate inicio, LocalDate fin,
                                            LocalTime horaInicio, LocalTime horaFin,
                                            Integer cantidadTotal, Integer cantidad,
                                            Integer cantidadMenores, TipoCliente tipoCliente) {
        CalculoCostoRequestDto dto = new CalculoCostoRequestDto();
        ReflectionTestUtils.setField(dto, "servicioId", servicioId);
        ReflectionTestUtils.setField(dto, "fechaInicio", inicio);
        ReflectionTestUtils.setField(dto, "fechaFin", fin);
        ReflectionTestUtils.setField(dto, "horaInicio", horaInicio);
        ReflectionTestUtils.setField(dto, "horaFin", horaFin);
        ReflectionTestUtils.setField(dto, "cantidadTotal", cantidadTotal);
        ReflectionTestUtils.setField(dto, "cantidad", cantidad);
        ReflectionTestUtils.setField(dto, "cantidadMenores", cantidadMenores);
        ReflectionTestUtils.setField(dto, "tipoCliente", tipoCliente);
        return dto;
    }

    @Test
    void servicioNoEncontrado_lanzaServicioNotFoundException() {
        when(consultaServicio.obtenerServicio(99L)).thenThrow(new ServicioNotFoundException(99L));

        CalculoCostoRequestDto dto = request(99L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3),
                null, null, null, null, null, null);

        assertThrows(ServicioNotFoundException.class, () -> service.calcularCosto(dto));
    }

    @Test
    void tipoClienteNull_usaParticular() {
        Servicio s = servicio(ModalidadPrecio.POR_DIA, new BigDecimal("1000"), new BigDecimal("700"), null, null);
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1),
                null, null, null, null, null, null);

        CalculoCostoResponseDto resultado = service.calcularCosto(dto);
        assertEquals(new BigDecimal("1000"), resultado.costoTotal());
    }

    @Test
    void tipoClienteSocio_usaPrecioSocio() {
        Servicio s = servicio(ModalidadPrecio.POR_DIA, new BigDecimal("1000"), new BigDecimal("700"), null, null);
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3),
                null, null, null, null, null, TipoCliente.SOCIO);

        CalculoCostoResponseDto resultado = service.calcularCosto(dto);
        assertEquals(new BigDecimal("2100"), resultado.costoTotal());
    }

    @Test
    void modalidadPorDia_calculaTresDias() {
        Servicio s = servicio(ModalidadPrecio.POR_DIA, new BigDecimal("1000"), new BigDecimal("700"), null, null);
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3),
                null, null, null, null, null, TipoCliente.PARTICULAR);

        CalculoCostoResponseDto resultado = service.calcularCosto(dto);
        assertEquals(new BigDecimal("3000"), resultado.costoTotal());
    }

    @Test
    void modalidadPorHora_calculaHoras() {
        Servicio s = servicio(ModalidadPrecio.POR_HORA, new BigDecimal("200"), new BigDecimal("150"), null, null);
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1),
                LocalTime.of(9, 0), LocalTime.of(13, 0),
                null, null, null, TipoCliente.PARTICULAR);

        CalculoCostoResponseDto resultado = service.calcularCosto(dto);
        assertEquals(new BigDecimal("800"), resultado.costoTotal());
    }

    @Test
    void modalidadPorUnidad_calculaCantidad() {
        Servicio s = servicio(ModalidadPrecio.POR_UNIDAD, new BigDecimal("500"), new BigDecimal("350"), null, null);
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1),
                null, null, null, 5, null, TipoCliente.PARTICULAR);

        CalculoCostoResponseDto resultado = service.calcularCosto(dto);
        assertEquals(new BigDecimal("2500"), resultado.costoTotal());
    }

    @Test
    void modalidadPorPersona_conExcedente() {
        Servicio s = servicio(ModalidadPrecio.POR_PERSONA, new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        // 7 total, 1 menor → 6 adultos equivalentes, excedente=2 → 2000 + 600 = 2600
        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1),
                null, null, 7, null, 1, TipoCliente.PARTICULAR);

        CalculoCostoResponseDto resultado = service.calcularCosto(dto);
        assertEquals(new BigDecimal("2600"), resultado.costoTotal());
    }

    @Test
    void modalidadPorDiaPorPersona_multiplicaPorDias() {
        Servicio s = servicio(ModalidadPrecio.POR_DIA_POR_PERSONA, new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        // 3 personas, 0 menores, excedente=0 → 2000 × 2 días = 4000
        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 2),
                null, null, 3, null, 0, TipoCliente.PARTICULAR);

        CalculoCostoResponseDto resultado = service.calcularCosto(dto);
        assertEquals(new BigDecimal("4000"), resultado.costoTotal());
    }

    @Test
    void fechaFinAnteriorAInicio_lanzaValidacionException() {
        Servicio s = servicio(ModalidadPrecio.POR_DIA, new BigDecimal("1000"), new BigDecimal("700"), null, null);
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 1),
                null, null, null, null, null, null);

        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class,
                () -> service.calcularCosto(dto));
        assertEquals(ReservaCodigoError.FECHA_FIN_ANTERIOR_A_INICIO.name(), ex.getCodigo());
    }

    @Test
    void porHora_sinHoras_lanzaValidacionException() {
        Servicio s = servicio(ModalidadPrecio.POR_HORA, new BigDecimal("200"), new BigDecimal("150"), null, null);
        when(consultaServicio.obtenerServicio(1L)).thenReturn(s);

        CalculoCostoRequestDto dto = request(1L,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1),
                null, null, null, null, null, TipoCliente.PARTICULAR);

        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class,
                () -> service.calcularCosto(dto));
        assertEquals(ReservaCodigoError.HORA_REQUERIDA_PARA_SERVICIO_POR_HORA.name(), ex.getCodigo());
    }
}
