package com.cipolflo.server.servicios.mapper;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.servicios.dto.ServicioReservaOcupacionDto;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioReservaOcupacionMapperTest {

    private Reserva crearReserva(Long id, LocalDate fechaEntrada, LocalDate fechaSalida) {
        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                1L,
                10L,
                Procedencia.CAMPING,
                fechaEntrada,
                fechaSalida,
                null, null, null, null, null,
                false
        );
        ReflectionTestUtils.setField(reserva, "id", id);
        return reserva;
    }

    @Test
    void deberiaMapearTodosLosCampos() {
        Reserva reserva = crearReserva(
                5L,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12)
        );

        ServicioReservaOcupacionDto dto = ServicioReservaOcupacionMapper.toOcupacionDto(reserva);

        assertEquals(5L, dto.reservaId());
        assertEquals(EstadoReserva.PENDIENTE, dto.estado());
        assertEquals(LocalDate.of(2026, 6, 10), dto.fechaInicio());
        assertEquals(LocalDate.of(2026, 6, 12), dto.fechaFin());
    }

    @Test
    void deberiaMapearListaCompleta() {
        Reserva reserva1 = crearReserva(1L, LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 12));
        Reserva reserva2 = crearReserva(2L, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 18));

        List<ServicioReservaOcupacionDto> dtos =
                ServicioReservaOcupacionMapper.toOcupacionDtoList(List.of(reserva1, reserva2));

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).reservaId());
        assertEquals(2L, dtos.get(1).reservaId());
    }

    @Test
    void deberiaRetornarListaVaciaCuandoNoHayReservas() {
        List<ServicioReservaOcupacionDto> dtos =
                ServicioReservaOcupacionMapper.toOcupacionDtoList(List.of());

        assertTrue(dtos.isEmpty());
    }
}
