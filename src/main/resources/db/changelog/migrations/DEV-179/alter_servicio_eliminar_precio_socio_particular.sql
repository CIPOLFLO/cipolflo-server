--liquibase formatted sql

--changeset cipolflo:DEV-179-alter-servicio-eliminar-precio-socio-particular
ALTER TABLE public.servicio
    DROP COLUMN precio_particular,
    DROP COLUMN precio_socio;

--rollback ALTER TABLE public.servicio ADD COLUMN precio_particular numeric(19,2) NOT NULL DEFAULT 0;
--rollback ALTER TABLE public.servicio ADD COLUMN precio_socio numeric(19,2) NOT NULL DEFAULT 0;
