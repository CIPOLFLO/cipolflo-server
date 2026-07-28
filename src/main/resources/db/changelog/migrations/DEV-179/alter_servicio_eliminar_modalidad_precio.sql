--liquibase formatted sql

--changeset cipolflo:DEV-179-alter-servicio-eliminar-modalidad-precio
ALTER TABLE public.servicio
    DROP COLUMN modalidad_precio;

--rollback ALTER TABLE public.servicio ADD COLUMN modalidad_precio character varying(50) NOT NULL DEFAULT 'POR_DIA';
