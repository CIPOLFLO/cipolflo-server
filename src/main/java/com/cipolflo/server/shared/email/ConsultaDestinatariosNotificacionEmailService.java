package com.cipolflo.server.shared.email;

import com.cipolflo.server.shared.email.domain.DestinatarioNotificacionEmail;
import com.cipolflo.server.shared.email.repository.DestinatarioNotificacionEmailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaDestinatariosNotificacionEmailService implements IConsultaDestinatariosNotificacionEmail {

    private final DestinatarioNotificacionEmailRepository repository;

    public ConsultaDestinatariosNotificacionEmailService(DestinatarioNotificacionEmailRepository repository) {
        this.repository = repository;
    }

    @Override
    public String destinatariosActivos() {
        List<String> emails = repository.findByActivoTrue().stream()
                .map(DestinatarioNotificacionEmail::getEmail)
                .toList();
        return emails.isEmpty() ? null : String.join(",", emails);
    }
}
