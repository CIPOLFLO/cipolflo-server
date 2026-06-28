package com.cipolflo.server.reservas;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.reservas.mapper.ReservaMapper;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaMapperTest {

    private Reserva crearReserva(Long clienteId) {
        Reserva r = Reserva.crear(
                TipoReserva.COMUN,
                clienteId,
                10L,
                Procedencia.CAMPING,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                null, null, 4, 1, null, null, null,
                "Llegan a las 14hs",
                false
        );
        ReflectionTestUtils.setField(r, "id", 42L);
        return r;
    }

    private ClienteDetalleReservaDto clienteDto() {
        return new ClienteDetalleReservaDto(12L, "Juan Pérez", "12345678", "099111111", "juan@mail.com", TipoCliente.SOCIO);
    }

    private ServicioDetalleReservaDto servicioDto() {
        return new ServicioDetalleReservaDto(3L, "Cabaña del río", Procedencia.CAMPING, ModalidadPrecio.POR_DIA);
    }

    @Test
    void deberiaMapearTodosLosCamposDeLaReserva() {
        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(crearReserva(12L), clienteDto(), servicioDto());

        assertEquals(42L, dto.getId());
        assertEquals(TipoReserva.COMUN, dto.getTipoReserva());
        assertEquals(EstadoReserva.PENDIENTE, dto.getEstado());
        assertEquals(Procedencia.CAMPING, dto.getProcedencia());
        assertEquals(LocalDate.of(2026, 8, 10), dto.getFechaEntrada());
        assertEquals(LocalDate.of(2026, 8, 15), dto.getFechaSalida());
        assertEquals(4, dto.getCantidadTotal());
        assertEquals(1, dto.getCantidadMenores());
        assertNull(dto.getCantidad());
        assertNull(dto.getImporte());
        assertNull(dto.getFormaPago());
        assertFalse(dto.getPago());
        assertFalse(dto.getRequiereDocumentacion());
        assertFalse(dto.getTieneDocumentacion());
        assertNull(dto.getRut());
        assertEquals("Llegan a las 14hs", dto.getNotas());
    }

    @Test
    void deberiaMapearClienteCuandoNoEsNull() {
        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(crearReserva(12L), clienteDto(), servicioDto());

        assertNotNull(dto.getCliente());
        assertEquals(12L, dto.getCliente().id());
        assertEquals("Juan Pérez", dto.getCliente().nombre());
        assertEquals("12345678", dto.getCliente().cedula());
        assertEquals("099111111", dto.getCliente().telefono());
        assertEquals("juan@mail.com", dto.getCliente().email());
        assertEquals(TipoCliente.SOCIO, dto.getCliente().tipoCliente());
    }

    @Test
    void deberiaMapearClienteComoNullCuandoEsNull() {
        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(crearReserva(null), null, servicioDto());

        assertNull(dto.getCliente());
    }

    @Test
    void deberiaMapearServicio() {
        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(crearReserva(12L), clienteDto(), servicioDto());

        assertNotNull(dto.getServicio());
        assertEquals(3L, dto.getServicio().id());
        assertEquals("Cabaña del río", dto.getServicio().nombre());
        assertEquals(Procedencia.CAMPING, dto.getServicio().procedencia());
        assertEquals(ModalidadPrecio.POR_DIA, dto.getServicio().modalidadPrecio());
    }

    @Test
    void deberiaMapearImporteYFormaPagoCuandoReservaEsPaga() {
        Reserva reserva = crearReserva(12L);
        reserva.confirmarPago(BigDecimal.valueOf(15000), FormaPago.EFECTIVO);

        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertEquals(BigDecimal.valueOf(15000), dto.getImporte());
        assertEquals(FormaPago.EFECTIVO, dto.getFormaPago());
        assertTrue(dto.getPago());
    }

    @Test
    void deberiaMapearCamposDeAuditoria() {
        Reserva reserva = crearReserva(12L);
        Instant ahora = Instant.now();
        ReflectionTestUtils.setField(reserva, "createdAt", ahora);
        ReflectionTestUtils.setField(reserva, "createdBy", "admin@test.com");

        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertEquals(ahora, dto.getCreatedAt());
        assertEquals("admin@test.com", dto.getCreatedBy());
    }

    @Test
    void deberiaNombreSerNullParaReservaComun() {
        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(crearReserva(12L), clienteDto(), servicioDto());

        assertNull(dto.getNombre());
    }

    @Test
    void deberiaMapearRequiereDocumentacionComoTrue() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                12L,
                10L,
                Procedencia.CAMPING,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                null, null, 2, 0, null, null, null,
                null,
                true
        );

        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertTrue(dto.getRequiereDocumentacion());
    }

    @Test
    void deberiaMapearCamposDeAuditoriaUpdatedAtYUpdatedBy() {
        Reserva reserva = crearReserva(12L);
        Instant ahora = Instant.now();
        ReflectionTestUtils.setField(reserva, "updatedAt", ahora);
        ReflectionTestUtils.setField(reserva, "updatedBy", "editor@test.com");

        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertEquals(ahora, dto.getUpdatedAt());
        assertEquals("editor@test.com", dto.getUpdatedBy());
    }

    @Test
    void deberiaMapearEstadoConfirmadoParaColaboracion() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO,
                null,
                10L,
                Procedencia.CAMPING,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3),
                null, null, null, null, null, "20123456-7", "Org Solidaria", null,
                false
        );

        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(reserva, null, servicioDto());

        assertEquals(EstadoReserva.CONFIRMADA, dto.getEstado());
        assertEquals(TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, dto.getTipoReserva());
        assertEquals(BigDecimal.ZERO, dto.getImporte());
        assertNull(dto.getCliente());
        assertEquals("20123456-7", dto.getRut());
        assertEquals("Org Solidaria", dto.getNombre());
    }

    @Test
void toExportFila_reservaComunConCliente_devuelveFilaConTodosLosCampos() {
    Reserva reserva = crearReserva(12L);

    List<String> fila = ReservaMapper.toExportFila(reserva, "Juan Pérez", "Cabaña del río");

    assertEquals(19, fila.size());
    assertEquals("42",               fila.get(0));   // id
    assertEquals("COMUN",            fila.get(1));   // tipoReserva
    assertEquals("PENDIENTE",        fila.get(2));   // estado
    assertEquals("CAMPING",          fila.get(3));   // procedencia
    assertEquals("2026-08-10",       fila.get(4));   // fechaEntrada
    assertEquals("2026-08-15",       fila.get(5));   // fechaSalida
    assertEquals("",                 fila.get(6));   // horaInicio null → ""
    assertEquals("",                 fila.get(7));   // horaFin null → ""
    assertEquals("Juan Pérez",       fila.get(8));   // nombreCliente
    assertEquals("Cabaña del río",   fila.get(9));   // nombreServicio
    assertEquals("",                 fila.get(10));  // importe null → ""
    assertEquals("",                 fila.get(11));  // formaPago null → ""
    assertEquals("No",               fila.get(12));  // pago = false
    assertEquals("4",                fila.get(13));  // cantidadTotal
    assertEquals("1",                fila.get(14));  // cantidadMenores
    assertEquals("",                 fila.get(15));  // cantidad null → ""
    assertEquals("No",               fila.get(16));  // requiereDocumentacion = false
    assertEquals("No",               fila.get(17));  // tieneDocumentacion = false
    assertEquals("Llegan a las 14hs", fila.get(18)); // notas
}

@Test
void toExportFila_reservaConPago_devuelveImporteYFormaPago() {
    Reserva reserva = crearReserva(12L);
    reserva.confirmarPago(BigDecimal.valueOf(15000), FormaPago.EFECTIVO);

    List<String> fila = ReservaMapper.toExportFila(reserva, "Juan Pérez", "Cabaña del río");

    assertEquals("15000",    fila.get(10));  // importe
    assertEquals("EFECTIVO", fila.get(11));  // formaPago
    assertEquals("Sí",       fila.get(12));  // pago = true
}

@Test
void toExportFila_colaboracionSinFinesLucro_usaNombreRutComoCliente() {
    Reserva reserva = Reserva.crear(
            TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO,
            null,
            10L,
            Procedencia.CAMPING,
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 3),
            null, null, null, null, null, "20123456-7", "Org Solidaria", null,
            false
    );

    List<String> fila = ReservaMapper.toExportFila(reserva, null, "Cabaña del río");

    assertEquals("Org Solidaria", fila.get(8));  // nombreRut como fallback
}

@Test
void toExportFila_clienteNullSinNombreRut_devuelveVacioEnCliente() {
    Reserva reserva = Reserva.crear(
            TipoReserva.COMUN,
            null,
            10L,
            Procedencia.CAMPING,
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 3),
            null, null, 2, 0, null, null, null, null,
            false
    );

    List<String> fila = ReservaMapper.toExportFila(reserva, null, "Cabaña del río");

    assertEquals("", fila.get(8));
}

@Test
void toExportFila_notasNulas_devuelveVacio() {
    Reserva reserva = Reserva.crear(
            TipoReserva.COMUN,
            12L,
            10L,
            Procedencia.CAMPING,
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 3),
            null, null, 2, 0, null, null, null, null,
            false
    );

    List<String> fila = ReservaMapper.toExportFila(reserva, "Juan Pérez", "Cabaña del río");

    assertEquals("", fila.get(18));
}

@Test
void toExportFila_requiereDocumentacion_devuelveSi() {
    Reserva reserva = Reserva.crear(
            TipoReserva.COMUN,
            12L,
            10L,
            Procedencia.CAMPING,
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 3),
            null, null, 2, 0, null, null, null, null,
            true
    );

    List<String> fila = ReservaMapper.toExportFila(reserva, "Juan Pérez", "Cabaña del río");

    assertEquals("Sí", fila.get(16));
}
}
