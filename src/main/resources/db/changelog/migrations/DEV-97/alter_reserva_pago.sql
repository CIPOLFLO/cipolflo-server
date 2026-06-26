--liquibase formatted sql

--changeset cipolflo:DEV-97-alter-reserva-agregar-campos-pago
ALTER TABLE public.reserva
    ADD COLUMN monto_impago NUMERIC(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN fecha_limite_pago TIMESTAMP NULL;

--rollback ALTER TABLE public.reserva DROP COLUMN fecha_limite_pago;
--rollback ALTER TABLE public.reserva DROP COLUMN monto_impago;

