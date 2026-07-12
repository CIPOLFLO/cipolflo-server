package com.cipolflo.server.integraciones.mensajeria.log;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste el log de un envío. Está separado del cliente del canal para que el
 * {@code @Transactional(REQUIRES_NEW)} funcione vía proxy: así el registro se
 * commitea en su propia transacción, independientemente del resultado del envío
 * o de cualquier transacción externa. Ver {@code EnvioEmailLogRegistrar}, su análogo
 * para mails.
 */
@Component
public class EnvioMensajeLogRegistrar {

    private final EnvioMensajeLogRepository repository;

    public EnvioMensajeLogRegistrar(EnvioMensajeLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(CanalMensaje canal, String destinatarioId, TipoEventoMensaje tipoEvento,
                          EstadoEnvioMensaje estado, String error) {
        EnvioMensajeLog log = new EnvioMensajeLog();
        log.setCanal(canal);
        log.setDestinatarioId(destinatarioId);
        log.setTipoEvento(tipoEvento);
        log.setEstado(estado);
        log.setError(error);
        repository.save(log);
    }
}
