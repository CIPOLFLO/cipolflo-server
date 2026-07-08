--liquibase formatted sql

--changeset cipolflo:DEV-98-add-requiere-sena-to-reserva
ALTER TABLE public.reserva
    ADD COLUMN requiere_sena boolean NOT NULL DEFAULT false;

--rollback ALTER TABLE public.reserva DROP COLUMN requiere_sena;
