package com.cipolflo.server.shared.email;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Registro de un intento de envío de email. Tabla append-only: se inserta una fila
 * por cada envío (ENVIADO o FALLIDO). Un scheduler de retención purga los registros
 * más antiguos que la ventana definida.
 */
@Entity
@Table(name = "envio_emails_logs")
@Getter
@Setter
public class EnvioEmailLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false)
    private String asunto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoEventoEmail tipoEvento;

    /** Id de la entidad que originó el mail (ej. la reserva). Nullable. */
    private Long referenciaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvioEmail estado;

    /** Detalle del error cuando el estado es FALLIDO. */
    @Column(columnDefinition = "TEXT")
    private String error;
}
