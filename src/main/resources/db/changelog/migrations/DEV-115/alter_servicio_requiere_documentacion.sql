--liquibase formatted sql

--changeset cipolflo:DEV-115-alter-servicio-requiere-documentacion
ALTER TABLE public.servicio
    ADD COLUMN requiere_documentacion BOOLEAN NULL;

--rollback ALTER TABLE public.servicio DROP COLUMN requiere_documentacion;
