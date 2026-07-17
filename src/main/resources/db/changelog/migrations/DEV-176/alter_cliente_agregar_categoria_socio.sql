--liquibase formatted sql

--changeset cipolflo:DEV-176-alter-cliente-agregar-categoria-socio
ALTER TABLE public.cliente
    ADD COLUMN categoria_socio character varying(50);

UPDATE public.cliente
SET categoria_socio = 'SOCIO_COMUN'
WHERE tipo = 'SOCIO'
  AND categoria_socio IS NULL;

--rollback ALTER TABLE public.cliente DROP COLUMN categoria_socio;