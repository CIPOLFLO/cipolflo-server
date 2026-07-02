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
                "Llegan a las 14hs",false,false
                
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
        assertEquals(EstadoReserva.CONFIRMADA, dto.getEstado());
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
        assertFalse(dto.getRequiereSena());
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
    void deberiaMapearCamposDeAuditoriaUpdatedAtYUpdatedBy() {
        Reserva reserva = crearReserva(12L);
        Instant ahora = Instant.now();
        ReflectionTestUtils.setField(reserva, "updatedAt", ahora);
        ReflectionTestUtils.setField(reserva, "updatedBy", "editor@test.com");

        ReservaDetalleResponseDto dto = ReservaMapper.toDetalleResponseDto(reserva, clienteDto(), servicioDto());

        assertEquals(ahora, dto.getUpdatedAt());
        assertEquals("editor@test.com", dto.getUpdatedBy());
    }

}