--liquibase formatted sql

--changeset cipolflo:DEV-139-add-campos-documentacion-sena-to-reserva
ALTER TABLE public.reserva
    ADD COLUMN requiere_sena boolean NOT NULL DEFAULT false;

--rollback ALTER TABLE public.reserva DROP COLUMN requiere_sena;