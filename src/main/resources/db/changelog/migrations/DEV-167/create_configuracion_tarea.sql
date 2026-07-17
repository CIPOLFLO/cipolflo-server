--liquibase formatted sql

--changeset cipolflo:DEV-167-create-configuracion-tarea
CREATE TABLE public.configuracion_tarea (
    clave varchar(60) PRIMARY KEY,
    valor varchar(255) NOT NULL,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    created_by character varying(255),
    updated_by character varying(255)
);
--rollback DROP TABLE IF EXISTS public.configuracion_tarea;

--changeset cipolflo:DEV-167-seed-configuracion-tarea-limpieza-reservas-finanzas
INSERT INTO public.configuracion_tarea (clave, valor) VALUES
    ('LIMPIEZA_RESERVAS_RETENCION_ANIOS', '2'),
    ('LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS', '3');
--rollback DELETE FROM public.configuracion_tarea WHERE clave IN ('LIMPIEZA_RESERVAS_RETENCION_ANIOS', 'LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS');
