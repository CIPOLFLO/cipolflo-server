package com.cipolflo.server.clientes.exception;

import java.util.List;

public class PagoCuotaNotFoundException extends RuntimeException {
    public PagoCuotaNotFoundException(List<Long> ids) {
        super("Pago de cuota no encontrado o no pertenece al socio para los ids: " + ids);
    }
}