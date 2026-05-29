package com.cipolflo.server.clientes.service;

import java.util.Collection;
import java.util.Map;

public interface IClienteService {
    Map<Long, String> getNombresByIds(Collection<Long> ids);
}
