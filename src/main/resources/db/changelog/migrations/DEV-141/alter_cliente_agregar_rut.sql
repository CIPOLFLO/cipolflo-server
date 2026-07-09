--liquibase formatted sql

--changeset cipolflo:DEV-141-alter-cliente-agregar-rut
ALTER TABLE public.cliente
    ADD COLUMN rut character varying(20) NULL;
ALTER TABLE public.cliente
    ADD CONSTRAINT cliente_rut_key UNIQUE (rut);

--rollback ALTER TABLE public.cliente DROP CONSTRAINT cliente_rut_key;
--rollback ALTER TABLE public.cliente DROP COLUMN rut;
