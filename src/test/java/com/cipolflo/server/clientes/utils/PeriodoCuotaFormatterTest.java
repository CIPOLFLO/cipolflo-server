package com.cipolflo.server.clientes.utils;

import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodoCuotaFormatterTest {

    @Test
    void formatearMeses_unSoloMes_sinConectores() {
        List<YearMonth> periodos = List.of(YearMonth.of(2025, 11));

        String resultado = PeriodoCuotaFormatter.formatearMeses(periodos);

        assertThat(resultado).isEqualTo("Noviembre de 2025");
    }

    @Test
    void formatearMeses_dosMesesMismoAnio_usaY() {
        List<YearMonth> periodos = List.of(YearMonth.of(2025, 11), YearMonth.of(2025, 12));

        String resultado = PeriodoCuotaFormatter.formatearMeses(periodos);

        assertThat(resultado).isEqualTo("Noviembre y Diciembre de 2025");
    }

    @Test
    void formatearMeses_tresOMasMesesMismoAnio_comasYUltimoConY() {
        List<YearMonth> periodos = List.of(
                YearMonth.of(2026, 1),
                YearMonth.of(2026, 2),
                YearMonth.of(2026, 3)
        );

        String resultado = PeriodoCuotaFormatter.formatearMeses(periodos);

        assertThat(resultado).isEqualTo("Enero, Febrero y Marzo de 2026");
    }

    @Test
    void formatearMeses_mesesCruzandoDosAnios_gruposSeparadosPorComa() {
        List<YearMonth> periodos = List.of(
                YearMonth.of(2025, 11),
                YearMonth.of(2025, 12),
                YearMonth.of(2026, 1),
                YearMonth.of(2026, 2),
                YearMonth.of(2026, 3)
        );

        String resultado = PeriodoCuotaFormatter.formatearMeses(periodos);

        assertThat(resultado).isEqualTo("Noviembre y Diciembre de 2025, Enero, Febrero y Marzo de 2026");
    }

    @Test
    void formatearMeses_ordenaCronologicamenteAunqueVenganDesordenados() {
        List<YearMonth> periodos = List.of(YearMonth.of(2025, 12), YearMonth.of(2025, 11));

        String resultado = PeriodoCuotaFormatter.formatearMeses(periodos);

        assertThat(resultado).isEqualTo("Noviembre y Diciembre de 2025");
    }

    @Test
    void armarMensajePago_unMes_usaSingular() {
        List<YearMonth> periodos = List.of(YearMonth.of(2025, 11));

        String mensaje = PeriodoCuotaFormatter.armarMensajePago(
                "Juan Pérez", periodos, new BigDecimal("1500.00"), MetodoCobro.TRANSFERENCIA);

        assertThat(mensaje).isEqualTo(
                "Estimado/a Juan Pérez: Se registra que hoy pagó la cuota perteneciente al mes de "
                        + "Noviembre de 2025, por el monto total de $1500.00. "
                        + "El pago fue realizado mediante Transferencia."
        );
    }

    @Test
    void armarMensajePago_masDeUnMes_usaPlural() {
        List<YearMonth> periodos = List.of(YearMonth.of(2025, 11), YearMonth.of(2025, 12));

        String mensaje = PeriodoCuotaFormatter.armarMensajePago(
                "Juan Pérez", periodos, new BigDecimal("3000.00"), MetodoCobro.EFECTIVO);

        assertThat(mensaje).isEqualTo(
                "Estimado/a Juan Pérez: Se registra que hoy pagó las cuotas pertenecientes a los meses de "
                        + "Noviembre y Diciembre de 2025, por el monto total de $3000.00. "
                        + "El pago fue realizado mediante Efectivo."
        );
    }
}