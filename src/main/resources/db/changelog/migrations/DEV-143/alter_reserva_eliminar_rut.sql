--liquibase formatted sql

--changeset cipolflo:DEV-143-alter-reserva-eliminar-rut
ALTER TABLE public.reserva
    DROP COLUMN rut;
ALTER TABLE public.reserva
    DROP COLUMN nombre_rut;
ALTER TABLE public.reserva
    ALTER COLUMN cliente_id SET NOT NULL;

--rollback ALTER TABLE public.reserva ALTER COLUMN cliente_id DROP NOT NULL;
--rollback ALTER TABLE public.reserva ADD COLUMN nombre_rut VARCHAR(255) NULL;
--rollback ALTER TABLE public.reserva ADD COLUMN rut VARCHAR(20) NULL;