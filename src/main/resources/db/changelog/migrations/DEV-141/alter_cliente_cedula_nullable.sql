--liquibase formatted sql

--changeset cipolflo:DEV-141-alter-cliente-cedula-nullable
ALTER TABLE public.cliente
    ALTER COLUMN cedula DROP NOT NULL;

--rollback ALTER TABLE public.cliente ALTER COLUMN cedula SET NOT NULL;
