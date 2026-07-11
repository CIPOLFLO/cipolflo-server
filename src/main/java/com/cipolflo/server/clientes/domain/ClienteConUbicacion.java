package com.cipolflo.server.clientes.domain;

/**
 * Implementada por los tipos de cliente que tienen datos de ubicación
 * (Socio y Empresa). Permite acceder a estos campos sin conocer el
 * subtipo concreto. Los getters los provee Lombok en cada clase.
 */
public interface ClienteConUbicacion {
    String getPais();
    String getDepartamento();
    String getCiudad();
    String getDireccion();
}
