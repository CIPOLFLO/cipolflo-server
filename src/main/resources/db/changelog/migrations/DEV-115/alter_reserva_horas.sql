--liquibase formatted sql

--changeset cipolflo:DEV-115-add-horas-reserva
ALTER TABLE public.reserva
    ADD COLUMN hora_inicio TIME NULL,
    ADD COLUMN hora_fin TIME NULL;

--rollback ALTER TABLE public.reserva DROP COLUMN hora_fin;
--rollback ALTER TABLE public.reserva DROP COLUMN hora_inicio;
