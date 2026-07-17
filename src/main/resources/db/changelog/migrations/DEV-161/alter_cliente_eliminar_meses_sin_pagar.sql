--liquibase formatted sql

--changeset cipolflo:DEV-161-alter-cliente-eliminar-meses-sin-pagar
ALTER TABLE public.cliente
    DROP COLUMN meses_sin_pagar;

--rollback ALTER TABLE public.cliente ADD COLUMN meses_sin_pagar integer NOT NULL DEFAULT 0;
