--liquibase formatted sql

-- TODO: temporal - almacena el nombre de la organización con RUT hasta definir manejo de clientes RUT
--changeset cipolflo:DEV-111-alter-reserva-nombre-rut
ALTER TABLE public.reserva
    ADD COLUMN nombre_rut VARCHAR(255) NULL;

--rollback ALTER TABLE public.reserva DROP COLUMN nombre_rut;
