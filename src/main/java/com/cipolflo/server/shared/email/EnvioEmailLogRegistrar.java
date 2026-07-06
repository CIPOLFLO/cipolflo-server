package com.cipolflo.server.shared.email;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste el log de un envío. Está separado de {@link EmailService} para que el
 * {@code @Transactional(REQUIRES_NEW)} funcione vía proxy: así el registro se
 * commitea en su propia transacción, independientemente del resultado del envío
 * o de cualquier transacción externa.
 */
@Component
public class EnvioEmailLogRegistrar {

    private final EnvioEmailLogRepository repository;

    public EnvioEmailLogRegistrar(EnvioEmailLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(SolicitudEmail solicitud, EstadoEnvioEmail estado, String error) {
        EnvioEmailLog log = new EnvioEmailLog();
        log.setDestinatario(solicitud.destinatario());
        log.setAsunto(solicitud.asunto());
        log.setTipoEvento(solicitud.tipoEvento());
        log.setReferenciaId(solicitud.referenciaId());
        log.setEstado(estado);
        log.setError(error);
        repository.save(log);
    }
}
