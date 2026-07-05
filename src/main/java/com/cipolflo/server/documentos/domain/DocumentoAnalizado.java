package com.cipolflo.server.documentos.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "documento_analizado")
@Getter
@Setter
public class DocumentoAnalizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre original del archivo subido por el usuario. */
    @Column(nullable = false)
    private String nombreArchivo;

    /** MIME type del archivo: application/pdf, image/jpeg, etc. */
    private String tipoContenido;

    /** Modelo de Azure usado: "prebuilt-invoice", "prebuilt-receipt", etc. */
    @Column(nullable = false)
    private String modeloUsado;

    /** Timestamp UTC del momento en que se realizó el análisis. */
    @Column(nullable = false)
    private LocalDate fechaAnalisis;


    /**
     * JSON completo devuelto por Azure. Se almacena como TEXT en PostgreSQL.
     * Contiene todos los campos extraídos, niveles de confianza y coordenadas.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String resultadoJson;

}

