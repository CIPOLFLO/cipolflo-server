package com.cipolflo.server.reservas.mapper;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaMapperTest {

    private static final BigDecimal IMPORTE_RESERVA = BigDecimal.valueOf(15000);

    private Reserva crearReserva(Long clienteId) {
        Reserva r = Reserva.crear(
                TipoReserva.COMUN,
                clienteId,
                10L,
                Procedencia.CAMPING,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                null,
                null,
                4,
                1,
                null,
                "Llegan a las 14hs",
                false,
                false,
                IMPORTE_RESERVA,
                null
        );
        ReflectionTestUtils.setField(r, "id", 42L);
        return r;
    }

    private ClienteDetalleReservaDto clienteDto() {
        return new ClienteDetalleReservaDto(
                12L,
                "Juan Pérez",
                "12345678",
                "099111111",
                "juan@mail.com",
                TipoCliente.SOCIO
        );
    }

    private ClienteDetalleReservaDto clienteEmpresaDto() {
        return new ClienteDetalleReservaDto(
                20L,
                "Org Solidaria S.A.",
                null,
                "099222222",
                "org@mail.com",
                TipoCliente.EMPRESA
        );
    }

    private ServicioDetalleReservaDto servicioDto() {
        return new ServicioDetalleReservaDto(
                3L,
                "Cabaña del río",
                Procedencia.CAMPING,
                ModalidadPrecio.POR_DIA
        );
    }

    @Test
    void deberiaMapearTodosLosCamposDeLaReserva() {
        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(crearReserva(12L), clienteDto(), servicioDto());

        assertEquals(42L, dto.getId());
        assertEquals(TipoReserva.COMUN, dto.getTipoReserva());
        assertEquals(EstadoReserva.CONFIRMADA, dto.getEstado());
        assertEquals(Procedencia.CAMPING, dto.getProcedencia());
        assertEquals(LocalDate.of(2026, 8, 10), dto.getFechaEntrada());
        assertEquals(LocalDate.of(2026, 8, 15), dto.getFechaSalida());
        assertEquals(4, dto.getCantidadTotal());
        assertEquals(1, dto.getCantidadMenores());
        assertNull(dto.getCantidad());
        assertEquals(IMPORTE_RESERVA, dto.getImporte());
        assertFalse(dto.getPago());
        assertFalse(dto.getRequiereDocumentacion());
        assertFalse(dto.getTieneDocumentacion());
        assertFalse(dto.getRequiereSena());
        assertNull(dto.getRut());
        assertEquals("Llegan a las 14hs", dto.getNotas());
    }

    @Test
    void deberiaMapearClienteCuandoNoEsNull() {
        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(crearReserva(12L), clienteDto(), servicioDto());

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
        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(crearReserva(null), null, servicioDto());

        assertNull(dto.getCliente());
    }

    @Test
    void deberiaMapearServicio() {
        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(crearReserva(12L), clienteDto(), servicioDto());

        assertNotNull(dto.getServicio());
        assertEquals(3L, dto.getServicio().id());
        assertEquals("Cabaña del río", dto.getServicio().nombre());
        assertEquals(Procedencia.CAMPING, dto.getServicio().procedencia());
        assertEquals(ModalidadPrecio.POR_DIA, dto.getServicio().modalidadPrecio());
    }

    @Test
    void deberiaMapearPagoCuandoReservaEsPaga() {
        Reserva reserva = crearReserva(12L);

        reserva.registrarPago(IMPORTE_RESERVA, true);

        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertEquals(IMPORTE_RESERVA, dto.getImporte());
        assertTrue(dto.getPago());
    }

    @Test
    void deberiaMapearCamposDeAuditoria() {
        Reserva reserva = crearReserva(12L);
        Instant ahora = Instant.now();
        ReflectionTestUtils.setField(reserva, "createdAt", ahora);
        ReflectionTestUtils.setField(reserva, "createdBy", "admin@test.com");

        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertEquals(ahora, dto.getCreatedAt());
        assertEquals("admin@test.com", dto.getCreatedBy());
    }

    @Test
    void deberiaMapearCamposDeAuditoriaUpdatedAtYUpdatedBy() {
        Reserva reserva = crearReserva(12L);
        Instant ahora = Instant.now();
        ReflectionTestUtils.setField(reserva, "updatedAt", ahora);
        ReflectionTestUtils.setField(reserva, "updatedBy", "editor@test.com");

        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertEquals(ahora, dto.getUpdatedAt());
        assertEquals("editor@test.com", dto.getUpdatedBy());
    }

    @Test
    void deberiaNombreSerNullParaReservaComun() {
        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(crearReserva(12L), clienteDto(), servicioDto());

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
                null,
                null,
                2,
                0,
                null,
                null,
                true,
                false,
                IMPORTE_RESERVA,
                null
        );

        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertTrue(dto.getRequiereDocumentacion());
    }

    @Test
    void deberiaMapearRequiereSenaComoTrue() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                12L,
                10L,
                Procedencia.CAMPING,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                null,
                null,
                2,
                0,
                null,
                null,
                null,
                null,
                false,
                true,
                IMPORTE_RESERVA,
                null
        );

        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertTrue(dto.getRequiereSena());
    }

    @Test
    void deberiaMapearEstadoConfirmadoParaColaboracionConClienteEmpresa() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO,
                20L,
                10L,
                Procedencia.CAMPING,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3),
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                BigDecimal.ZERO,
                null
        );

        ReservaDetalleResponseDto dto =
                ReservaMapper.toDetalleResponseDto(reserva, clienteEmpresaDto(), servicioDto());

        assertEquals(EstadoReserva.CONFIRMADA, dto.getEstado());
        assertEquals(TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, dto.getTipoReserva());
        assertEquals(BigDecimal.ZERO, dto.getImporte());
        assertNotNull(dto.getCliente());
        assertEquals(TipoCliente.EMPRESA, dto.getCliente().tipoCliente());
        assertEquals("Org Solidaria S.A.", dto.getCliente().nombre());
    }

    @Test
    void toExportFila_reservaComunConCliente_devuelveFilaConTodosLosCampos() {
        Reserva reserva = crearReserva(12L);

        List<String> fila = ReservaMapper.toExportFila(reserva, "Juan Pérez", "Cabaña del río");

        assertEquals(18, fila.size());
        assertEquals("42",                fila.get(0));   // id
        assertEquals("Común",             fila.get(1));   // tipoReserva
        assertEquals("Confirmada",        fila.get(2));   // estado
        assertEquals("Camping",           fila.get(3));   // procedencia
        assertEquals("Cabaña del río",    fila.get(4));   // nombreServicio
        assertEquals("Juan Pérez",        fila.get(5));   // nombreCliente
        assertEquals("2026-08-10",        fila.get(6));   // fechaEntrada
        assertEquals("2026-08-15",        fila.get(7));   // fechaSalida
        assertEquals("",                  fila.get(8));   // horaInicio null → ""
        assertEquals("",                  fila.get(9));   // horaFin null → ""
        assertEquals("15000",             fila.get(10));  // importe
        assertEquals("No",                fila.get(11));  // pago = false
        assertEquals("4",                 fila.get(12));  // cantidadTotal
        assertEquals("1",                 fila.get(13));  // cantidadMenores
        assertEquals("",                  fila.get(14));  // cantidad null → ""
        assertEquals("No",                fila.get(15));  // requiereDocumentacion = false
        assertEquals("No",                fila.get(16));  // tieneDocumentacion = false
        assertEquals("Llegan a las 14hs", fila.get(17));  // notas
    }

    @Test
    void toExportFila_reservaPaga_devuelvePagoSi() {
        Reserva reserva = crearReserva(12L);
        reserva.registrarPago(IMPORTE_RESERVA, true);

        List<String> fila = ReservaMapper.toExportFila(reserva, "Juan Pérez", "Cabaña del río");

        assertEquals("15000", fila.get(10));  // importe
        assertEquals("Sí",    fila.get(11));  // pago = true
    }

    @Test
    void toExportFila_nombreClienteNoProvisto_devuelveVacioEnCliente() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                12L,
                10L,
                Procedencia.CAMPING,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3),
                null, null, 2, 0, null, null, false,
                null, null
        );

        List<String> fila = ReservaMapper.toExportFila(reserva, null, "Cabaña del río");

        assertEquals("", fila.get(5));
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
                null, null, 2, 0, null, null, false,
                null, null
        );

        List<String> fila = ReservaMapper.toExportFila(reserva, "Juan Pérez", "Cabaña del río");

        assertEquals("", fila.get(17));
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
                null, null, 2, 0, null, null, true,
                null, null
        );

        List<String> fila = ReservaMapper.toExportFila(reserva, "Juan Pérez", "Cabaña del río");

        assertEquals("Sí", fila.get(15));
    }
}