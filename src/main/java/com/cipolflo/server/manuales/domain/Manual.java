package com.cipolflo.server.manuales.domain;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Catálogo cerrado de los manuales que el backend puede servir.
 *
 * <p>Los PDF son archivos estáticos versionados junto al código, en
 * {@code src/main/resources/manuales/&lt;carpeta-categoria&gt;/}. El enum es la única
 * fuente de verdad: la API expone la {@link #getClave() clave} y nunca un nombre de
 * archivo recibido del cliente, de modo que no hay forma de pedir un recurso
 * arbitrario del classpath (path traversal).</p>
 *
 * <p><b>Versionado:</b> {@code version} y {@code fechaActualizacion} viajan en el mismo
 * commit que el PDF, por eso viven acá y no en base de datos. Un manual todavía no
 * publicado se declara sin esos datos; al subir el PDF hay que completarlos. La
 * coherencia entre "existe el archivo" y "tiene metadata" está cubierta por
 * {@code ManualCatalogoTest}, así que un PDF nuevo sin versión rompe el build.</p>
 *
 * <p>Para dar de alta un manual nuevo alcanza con agregar una constante acá y dejar
 * el PDF con ese nombre en la carpeta correspondiente.</p>
 */
public enum Manual {

    AUTH0_CONFIGURACION(
            CategoriaManual.TECNICO,
            "auth0-configuracion",
            "Manual Técnico - Configuración de Auth0",
            "manual-tecnico-auth0-configuracion.pdf"),

    AUTH0_NUEVO_USUARIO(
            CategoriaManual.TECNICO,
            "auth0-nuevo-usuario",
            "Manual Técnico - Alta de Nuevo Usuario en Auth0",
            "manual-tecnico-auth0-nuevo-usuario.pdf"),

    AZURE_DOC_INTELLIGENCE(
            CategoriaManual.TECNICO,
            "azure-doc-intelligence",
            "Manual Técnico - Azure Document Intelligence",
            "manual-tecnico-azure-doc-intelligence.pdf"),

    BOT_TELEGRAM(
            CategoriaManual.TECNICO,
            "bot-telegram",
            "Manual Técnico - Bot de Telegram",
            "manual-tecnico-bot-telegram.pdf",
            "1.0",
            LocalDate.of(2026, 7, 31)),

    INICIO_SESION(
            CategoriaManual.USUARIO,
            "inicio-sesion",
            "Manual de Inicio de Sesión",
            "manual-usuario-inicio-sesion.pdf"),

    RESERVAS(
            CategoriaManual.USUARIO,
            "reservas",
            "Manual de Módulo Reservas",
            "manual-usuario-reservas.pdf",
            "1.0",
            LocalDate.of(2026, 7, 31)),

    CLIENTES(
            CategoriaManual.USUARIO,
            "clientes",
            "Manual de Módulo Clientes",
            "manual-usuario-clientes.pdf"),

    SERVICIOS(
            CategoriaManual.USUARIO,
            "servicios",
            "Manual de Módulo Servicios",
            "manual-usuario-servicios.pdf",
            "1.0",
            LocalDate.of(2026, 7, 31)),

    FINANZAS(
            CategoriaManual.USUARIO,
            "finanzas",
            "Manual de Módulo Finanzas",
            "manual-usuario-finanzas.pdf"),

    AJUSTES(
            CategoriaManual.USUARIO,
            "ajustes",
            "Manual de Módulo Ajustes",
            "manual-usuario-ajustes.pdf");

    private static final String CARPETA_RAIZ = "manuales";

    private final CategoriaManual categoria;
    private final String clave;
    private final String titulo;
    private final String nombreArchivo;
    private final String version;
    private final LocalDate fechaActualizacion;

    /** Manual todavía no publicado: el PDF aún no está en el repositorio. */
    Manual(CategoriaManual categoria, String clave, String titulo, String nombreArchivo) {
        this(categoria, clave, titulo, nombreArchivo, null, null);
    }

    Manual(CategoriaManual categoria,
           String clave,
           String titulo,
           String nombreArchivo,
           String version,
           LocalDate fechaActualizacion) {

        this.categoria = categoria;
        this.clave = clave;
        this.titulo = titulo;
        this.nombreArchivo = nombreArchivo;
        this.version = version;
        this.fechaActualizacion = fechaActualizacion;
    }

    public CategoriaManual getCategoria() {
        return categoria;
    }

    /** Identificador estable usado en la URL. */
    public String getClave() {
        return clave;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    /**
     * Nombre con el que se le ofrece el archivo al usuario al descargar.
     *
     * <p>Usa el título en lugar del nombre interno para que quede legible en la carpeta
     * de descargas. Se sanean los caracteres que Windows no admite en un nombre de
     * archivo; los acentos se preservan y viajan en el header como UTF-8.</p>
     */
    public String getNombreArchivoDescarga() {
        return titulo.replaceAll("[\\\\/:*?\"<>|]", "-") + ".pdf";
    }

    /** Versión declarada del manual, o {@code null} si todavía no fue publicado. */
    public String getVersion() {
        return version;
    }

    /** Fecha de la última revisión del PDF, o {@code null} si todavía no fue publicado. */
    public LocalDate getFechaActualizacion() {
        return fechaActualizacion;
    }

    /** Indica si el manual tiene metadata de publicación declarada. */
    public boolean tieneVersionDeclarada() {
        return version != null && fechaActualizacion != null;
    }

    /** Ruta del PDF dentro del classpath. */
    public String getRutaClasspath() {
        return CARPETA_RAIZ + "/" + categoria.getCarpeta() + "/" + nombreArchivo;
    }

    public static Optional<Manual> porClave(String clave) {
        if (clave == null || clave.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(manual -> manual.clave.equalsIgnoreCase(clave.trim()))
                .findFirst();
    }

    public static List<Manual> porCategoria(CategoriaManual categoria) {
        return Arrays.stream(values())
                .filter(manual -> categoria == null || manual.categoria == categoria)
                .toList();
    }
}
