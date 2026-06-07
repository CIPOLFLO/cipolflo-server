package com.cipolflo.server.clientes.domain;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.dto.RegistroSocioRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@DiscriminatorValue("SOCIO")
@Getter
@Setter
@NoArgsConstructor
public class Socio extends Cliente {

    private Integer numeroSocio;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSocio estado;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false)
    private String departamento;

    @Column(nullable = false)
    private String ciudad;

    @Column()
    private String direccion;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    private LocalDate fechaUltimoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoCobro metodoCobro;

    @Column(nullable = false)
    private Integer mesesSinPagar = 0;

    public void incrementarMesesSinPagar() {
        this.mesesSinPagar++;
        if (this.mesesSinPagar >= 3) {
            this.estado = EstadoSocio.INACTIVO;
        }
    }

    public void darDeBaja() {
        this.estado = EstadoSocio.DE_BAJA;
    }

    public void modificar(String cedula, String nombreCompleto, String telefono, String mail, String notas,
                          LocalDate fechaNacimiento, String pais, String departamento,
                          String ciudad, String direccion, MetodoCobro metodoCobro) {
        this.setCedula(cedula);
        super.modificar(nombreCompleto, telefono, mail, notas);
        this.fechaNacimiento = fechaNacimiento;
        this.pais = pais;
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.metodoCobro = metodoCobro;
    }

    public static Socio registrar(
            RegistroSocioRequestDto dto,
            String cedulaNormalizada,
            String mailNormalizado,
            Integer numeroSocio
    ) {
        Socio socio = new Socio();
        socio.setCedula(cedulaNormalizada);
        socio.setNombreCompleto(dto.getNombre());
        socio.setTelefono(dto.getTelefono());
        socio.setMail(mailNormalizado);
        socio.setFechaNacimiento(dto.getFechaNacimiento());
        socio.setMetodoCobro(dto.getMetodoCobro());
        socio.setPais(dto.getPais());
        socio.setDepartamento(dto.getDepartamento());
        socio.setCiudad(dto.getCiudad());
        socio.setDireccion(dto.getDireccion());
        socio.setNotas(dto.getObservaciones());
        socio.setNumeroSocio(numeroSocio);
        socio.setEstado(EstadoSocio.ACTIVO);
        socio.setFechaIngreso(LocalDate.now(ZoneId.systemDefault()));
        socio.setMesesSinPagar(0);
        return socio;
    }
}
