--liquibase formatted sql

--changeset cipolflo:DEV-133-create-documento-analizado
CREATE TABLE public.documento_analizado (
                                            id bigserial PRIMARY KEY,
                                            nombre_archivo varchar(255) NOT NULL,
                                            tipo_contenido varchar(255),
                                            modelo_usado varchar(100) NOT NULL,
                                            fecha_analisis date NOT NULL,
                                            resultado_json text NOT NULL
);

--rollback DROP TABLE IF EXISTS public.documento_analizado;