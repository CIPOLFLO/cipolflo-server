--liquibase formatted sql

--changeset cipolflo:DEV-98-drop-requiere-documentacion-from-servicio
ALTER TABLE public.servicio
    DROP COLUMN requiere_documentacion;

--rollback ALTER TABLE public.servicio ADD COLUMN requiere_documentacion BOOLEAN NULL;
