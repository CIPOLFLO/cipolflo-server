--liquibase formatted sql

--changeset cipolflo:DEV-138-alter-servicio-agregar-costo-persona-extra
ALTER TABLE public.servicio
    ADD COLUMN costo_persona_extra DECIMAL(10,2) NULL;

--rollback ALTER TABLE public.servicio DROP COLUMN costo_persona_extra;
