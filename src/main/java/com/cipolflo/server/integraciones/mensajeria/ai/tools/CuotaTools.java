package com.cipolflo.server.integraciones.mensajeria.ai.tools;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.BusquedaCedulaResponseDto;
import com.cipolflo.server.clientes.dto.BusquedaRutResponseDto;
import com.cipolflo.server.clientes.dto.EstadoSocioResponseDto;
import com.cipolflo.server.clientes.dto.UltimaCuotaDto;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.clientes.service.IConsultaClienteDetalle;
import com.cipolflo.server.clientes.service.IPagoCuotaService;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tool de solo lectura para consultar el estado de cuota de socios de la asociación.
 * No desambigua homónimos: si el nombre o identificador matchea más de un cliente, responde
 * por todos (ver regla transversal del paquete {@code ai/tools}). No lanza excepciones ante
 * entrada inválida o ambigua: devuelve un texto explicativo para que el modelo pueda repreguntar.
 */
@Component
public class CuotaTools {

    private static final int MAX_COINCIDENCIAS = 10;
    private static final int LARGO_CEDULA_MIN = 7;
    private static final int LARGO_CEDULA_MAX = 8;
    private static final int LARGO_RUT = 12;

    private final IConsultaClienteDetalle consultaClienteDetalle;
    private final IClienteService clienteService;
    private final IPagoCuotaService pagoCuotaService;

    public CuotaTools(IConsultaClienteDetalle consultaClienteDetalle, IClienteService clienteService,
                      IPagoCuotaService pagoCuotaService) {
        this.consultaClienteDetalle = consultaClienteDetalle;
        this.clienteService = clienteService;
        this.pagoCuotaService = pagoCuotaService;
    }

    @Tool(description = "Consulta el estado de cuota de los socios de la asociación. Se puede buscar por nombre "
            + "o por identificador (cédula o RUT). Devuelve, para cada coincidencia, el estado actual del socio, "
            + "los meses sin pagar y la última cuota paga. No sirve para consultar el pago de una reserva.")
    public String consultarEstadoCuota(
            @ToolParam(description = "Nombre completo o parcial del socio", required = false) String nombre,
            @ToolParam(description = "Cédula o RUT, con o sin puntos y guión", required = false) String identificador) {

        boolean sinNombre = nombre == null || nombre.isBlank();
        boolean sinIdentificador = identificador == null || identificador.isBlank();
        if (sinNombre && sinIdentificador) {
            return "Necesito un nombre o un número de cédula/RUT para buscar al socio.";
        }

        return sinIdentificador ? responderPorNombre(nombre) : responderPorIdentificador(identificador);
    }

    private String responderPorNombre(String nombre) {
        List<Long> ids = consultaClienteDetalle.getIdsByNombre(nombre);
        if (ids.isEmpty()) {
            return "No encontré ningún cliente con ese nombre.";
        }

        boolean hayMas = ids.size() > MAX_COINCIDENCIAS;
        List<Long> aMostrar = hayMas ? ids.subList(0, MAX_COINCIDENCIAS) : ids;

        String cuerpo = aMostrar.stream()
                .map(consultaClienteDetalle::getDetallClienteSimple)
                .map(this::aClienteEncontrado)
                .map(this::bloqueEstadoCuota)
                .collect(Collectors.joining("\n\n"));

        if (!hayMas) {
            return cuerpo;
        }
        return cuerpo + "\n\nHay más resultados de los que puedo mostrar (" + ids.size()
                + " en total). Afiná la búsqueda para acotarlos.";
    }

    private String responderPorIdentificador(String identificador) {
        String soloDigitos = CedulaNormalizador.normalizar(identificador);
        try {
            if (soloDigitos.length() == LARGO_RUT) {
                BusquedaRutResponseDto dto = clienteService.buscarPorRut(identificador);
                return bloqueEstadoCuota(
                        new ClienteEncontrado(dto.getId(), dto.getNombre(), dto.getRut(), dto.getTipoCliente()));
            }
            if (soloDigitos.length() >= LARGO_CEDULA_MIN && soloDigitos.length() <= LARGO_CEDULA_MAX) {
                BusquedaCedulaResponseDto dto = clienteService.buscarPorCedula(identificador);
                return bloqueEstadoCuota(
                        new ClienteEncontrado(dto.getId(), dto.getNombre(), dto.getCedula(), dto.getTipoCliente()));
            }
        } catch (ClienteValidacionException | ClienteNotFoundException e) {
            return "No encontré ningún cliente con ese identificador.";
        }
        return "No encontré ningún cliente con ese identificador.";
    }

    private ClienteEncontrado aClienteEncontrado(ClienteDetalleReservaDto dto) {
        return new ClienteEncontrado(dto.id(), dto.nombre(), dto.documento(), dto.tipoCliente());
    }

    private String bloqueEstadoCuota(ClienteEncontrado cliente) {
        String encabezado = cliente.nombre() + " (" + cliente.documento() + ")";

        if (cliente.tipoCliente() != TipoCliente.SOCIO) {
            return encabezado + ": no es socio, no tiene cuotas.";
        }

        EstadoSocioResponseDto estado = clienteService.consultarEstadoSocio(cliente.id());
        UltimaCuotaDto ultimaCuota = pagoCuotaService.calcularUltimaCuotaPaga(cliente.id());
        String descripcionUltimaCuota = ultimaCuota != null ? ultimaCuota.descripcion() : "nunca pagó una cuota";

        return encabezado + ": estado " + estado.getEstado().name()
                + ", " + estado.getMesesSinPagar() + " meses sin pagar"
                + ", última cuota paga: " + descripcionUltimaCuota + ".";
    }

    private record ClienteEncontrado(Long id, String nombre, String documento, TipoCliente tipoCliente) {
    }
}
