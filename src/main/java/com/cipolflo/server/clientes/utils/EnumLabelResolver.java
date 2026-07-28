package com.cipolflo.server.clientes.utils;

import java.util.Arrays;
import java.util.Optional;

public class EnumLabelResolver {

    private EnumLabelResolver() {}

    public static <E extends Enum<E>> Optional<E> resolverPorLabel(Class<E> tipo, String texto) {
        if (texto == null || texto.isBlank()) return Optional.empty();
        String normalizado = texto.trim();
        return Arrays.stream(tipo.getEnumConstants())
                .filter(valor -> valor.toString().equalsIgnoreCase(normalizado)
                        || valor.name().equalsIgnoreCase(normalizado))
                .findFirst();
    }
}
