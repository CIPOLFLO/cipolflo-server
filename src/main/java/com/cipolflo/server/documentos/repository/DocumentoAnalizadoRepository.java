package com.cipolflo.server.documentos.repository;


import com.cipolflo.server.documentos.domain.DocumentoAnalizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface DocumentoAnalizadoRepository extends JpaRepository<DocumentoAnalizado, Long> {

    /**
     * Buscar todos los documentos procesados con un modelo específico.
     */
    List<DocumentoAnalizado> findByModeloUsado(String modeloUsado);

    /**
     * Buscar por nombre de archivo (útil para evitar reprocesar el mismo archivo).
     */
    List<DocumentoAnalizado> findByNombreArchivo(String nombreArchivo);

}