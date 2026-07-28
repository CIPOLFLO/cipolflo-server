package com.cipolflo.server.shared.email.domain;

import com.cipolflo.server.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Email habilitado a recibir notificaciones administrativas (reporte semanal de reservas,
 * avisos de cancelación, etc.). El alta, modificación, habilitación y baja se administran
 * desde la pantalla de Ajustes (módulo {@code ajustes}), que compone este repositorio en
 * lugar de duplicar la tabla.
 */
@Entity
@Table(name = "destinatario_notificacion_email")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DestinatarioNotificacionEmail extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Nombre de la persona, para saber de quién es el email sin depender de la dirección. */
    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private Boolean activo = true;

    public static DestinatarioNotificacionEmail registrar(String email, String alias) {
        DestinatarioNotificacionEmail destinatario = new DestinatarioNotificacionEmail();
        destinatario.email = email;
        destinatario.alias = alias;
        destinatario.activo = true;
        return destinatario;
    }

    public void modificar(String alias) {
        this.alias = alias;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }
}
