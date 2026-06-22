--liquibase formatted sql

--changeset cipolflo:DEV-115-alter-reserva-nueva-reserva
ALTER TABLE public.reserva
    ADD COLUMN tipo_reserva VARCHAR(100) NOT NULL DEFAULT 'COMUN',
    ADD COLUMN cantidad INTEGER NULL,
    ADD COLUMN rut VARCHAR(20) NULL;

ALTER TABLE public.reserva
    ALTER COLUMN cliente_id DROP NOT NULL;

--rollback ALTER TABLE public.reserva ALTER COLUMN cliente_id SET NOT NULL;
--rollback ALTER TABLE public.reserva DROP COLUMN rut;
--rollback ALTER TABLE public.reserva DROP COLUMN cantidad;
--rollback ALTER TABLE public.reserva DROP COLUMN tipo_reserva;

--changeset cipolflo:DEV-115-rename-reserva-columns
ALTER TABLE public.reserva
    RENAME COLUMN cantidad_personas TO cantidad_total;

ALTER TABLE public.reserva
    RENAME COLUMN documentacion TO requiere_documentacion;

ALTER TABLE public.reserva
    ADD COLUMN tiene_documentacion BOOLEAN NOT NULL DEFAULT false;

--rollback ALTER TABLE public.reserva DROP COLUMN tiene_documentacion;
--rollback ALTER TABLE public.reserva RENAME COLUMN requiere_documentacion TO documentacion;
--rollback ALTER TABLE public.reserva RENAME COLUMN cantidad_total TO cantidad_personas;
