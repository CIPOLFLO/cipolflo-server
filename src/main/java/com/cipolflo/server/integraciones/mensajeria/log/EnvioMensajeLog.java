package com.cipolflo.server.integraciones.mensajeria.log;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Registro de un intento de envío de mensaje por un canal de mensajería. Tabla
 * append-only, espejo de {@code envio_emails_logs}: se inserta una fila por cada envío
 * (ENVIADO o FALLIDO). Es la única trazabilidad disponible cuando el canal falla, porque
 * en ese caso no hay forma de avisarle al usuario.
 */
@Entity
@Table(name = "envio_mensajes_log")
@Getter
@Setter
public class EnvioMensajeLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalMensaje canal;

    @Column(nullable = false, length = 64)
    private String destinatarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoEventoMensaje tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvioMensaje estado;

    /** Detalle del error cuando el estado es FALLIDO. */
    @Column(columnDefinition = "TEXT")
    private String error;
}
